/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.statusneo.vms.service;

import com.statusneo.vms.dto.DirectorySyncResult;
import com.statusneo.vms.dto.UserResponse;
import com.statusneo.vms.dto.UsersDeltaResponse;
import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.exception.DirectorySyncException;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public  class GraphDirectoryService {

    private static final Logger logger = LoggerFactory.getLogger(GraphDirectoryService.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestClient restClient;
    private final EmployeeRepository employeeRepository;
    private final EmployeeNameCache employeeNameCache;

    public GraphDirectoryService(
            OAuth2AuthorizedClientManager authorizedClientManager,
            RestClient restClient,
            EmployeeRepository employeeRepository,
            EmployeeNameCache employeeNameCache) {
        this.authorizedClientManager = authorizedClientManager;
        this.restClient = restClient;
        this.employeeRepository = employeeRepository;
        this.employeeNameCache = employeeNameCache;
    }

    /**
     * Sync all users from Microsoft Graph into the employee table.
     * This method processes users page-by-page. For each page we:
     *  - collect emails from the page
     *  - query existing employees with a single IN query to avoid N+1
     *  - prepare new Employee entities for missing emails and update names for existing ones
     *  - call saveAll to perform batch inserts/updates
     *
     * It returns the total number of upserts performed and the delta link for future incremental syncs.
     *
     * @return DirectorySyncResult containing the number of upserts and the delta link (if available)
     */
    public DirectorySyncResult syncAllUsersToEmployees() {
        String token = getAccessToken();
        String nextLink = "https://graph.microsoft.com/v1.0/users/delta?$select=id,displayName,mail,userPrincipalName,accountEnabled";
        List<String> namesForCache = new ArrayList<>();
        int processed = 0;
        String deltaLink = null;

        while (nextLink != null) {
            // Log the outgoing HTTP request (method + url) while redacting sensitive headers
            logger.info("Graph API request: method=GET url={}", nextLink);
            try {
                ResponseEntity<UsersDeltaResponse> response = restClient.get()
                        .uri(nextLink)
                        .headers(httpHeaders -> {
                            httpHeaders.setBearerAuth(token);
                            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                        })
                        .retrieve()
                        .toEntity(UsersDeltaResponse.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    String errorMessage = String.format("Graph API request failed with status code %d for URL: %s",
                            response.getStatusCode().value(), nextLink);
                    logger.error(errorMessage);
                    throw new DirectorySyncException(errorMessage);
                }
                UsersDeltaResponse body = response.getBody();
                if (body == null) {
                    String errorMessage = String.format("Graph API returned null response body for URL: %s", nextLink);
                    logger.error(errorMessage);
                    throw new DirectorySyncException(errorMessage);
                }

                int pageProcessed = processUsersResponse(body, namesForCache);
                processed += pageProcessed;

                // Check for deltaLink (appears on final page when delta query completes)
                if (body.getDeltaLink() != null) {
                    deltaLink = body.getDeltaLink();
                    logger.info("Delta query completed. Delta link received for future incremental syncs");
                }

                nextLink = body.getNextLink();
            } catch (RestClientException e) {
                String errorMessage = String.format("Graph API request failed for URL: %s", nextLink);
                logger.error(errorMessage, e);
                throw new DirectorySyncException(errorMessage, e);
            }
        }

        employeeNameCache.clear();
        employeeNameCache.bulkInsert(namesForCache);
        int upserts = processed;
        logger.info("Employee sync complete. Upserts: {}", upserts);
        return new DirectorySyncResult(upserts, deltaLink);
    }

    /**
     * Process a page of users from the Graph API response.
     * Extracts users, determines which employees need to be created or updated,
     * performs the database operations, and collects names for cache updates.
     *
     * @param body the Graph API response body containing users
     * @param namesForCache list to collect employee names for cache updates
     * @return number of employees processed (upserted)
     */
    private int processUsersResponse(UsersDeltaResponse body, List<String> namesForCache) {
        List<UserResponse> users = body.getValue();
        if (users == null || users.isEmpty()) {
            return 0;
        }

        // Collect GUIDs from the page (filter out nulls)
        Set<String> guidsInPage = users.stream()
                .map(UserResponse::getId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toSet());

        if (guidsInPage.isEmpty()) {
            return 0;
        }

        // Load existing employees for the page by GUID in one query
        List<Employee> existing = employeeRepository.findByGuidIn(guidsInPage);
        Map<String, Employee> existingByGuid = existing.stream()
                .filter(e -> e.getGuid() != null)
                .collect(Collectors.toMap(
                        Employee::getGuid,
                        e -> e,
                        (e1, e2) -> e1 // In case of duplicates, keep first
                ));

        List<Employee> toSave = new ArrayList<>();

        // For each user, decide whether to insert new or update existing entity
        for (UserResponse user : users) {
            String userId = user.getId();
            if (userId == null || userId.isBlank()) {
                continue; // Skip users without GUID
            }

            String displayName = user.getDisplayName();
            String mail = user.getMail();
            String upn = user.getUserPrincipalName();
            String email = mail != null && !mail.isBlank() ? mail : upn;
            
            if (email == null || email.isBlank() || displayName == null || displayName.isBlank()) {
                continue;
            }

            Employee existingEmployee = existingByGuid.get(userId);
            
            if (existingEmployee != null) {
                // Update existing entity with all fields
                boolean needsUpdate = false;
                if (!displayName.equals(existingEmployee.getName())) {
                    existingEmployee.setName(displayName);
                    needsUpdate = true;
                }
                if (!email.equalsIgnoreCase(existingEmployee.getEmail())) {
                    existingEmployee.setEmail(email);
                    needsUpdate = true;
                }
                if (upn != null && !upn.equals(existingEmployee.getUserPrincipalName())) {
                    existingEmployee.setUserPrincipalName(upn);
                    needsUpdate = true;
                }
                if (user.getAccountEnabled() != null && !user.getAccountEnabled().equals(existingEmployee.getAccountEnabled())) {
                    existingEmployee.setAccountEnabled(user.getAccountEnabled());
                    needsUpdate = true;
                }
                if (needsUpdate) {
                    toSave.add(existingEmployee);
                }
            } else {
                // Create new employee entity
                Employee e = new Employee();
                e.setEmail(email);
                e.setName(displayName);
                e.setGuid(userId);
                e.setUserPrincipalName(upn);
                e.setAccountEnabled(user.getAccountEnabled());
                toSave.add(e);
            }
        }

        if (toSave.isEmpty()) {
            return 0;
        }

        List<Employee> saved = employeeRepository.saveAll(toSave);
        // Update cache and processed count
        for (Employee s : saved) {
            namesForCache.add(s.getName());
        }
        return saved.size();
    }

    private String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("azure")
                .principal("principal")
                .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);
        if (client == null || client.getAccessToken() == null) {
            String errorMessage = "Failed to obtain Graph access token";
            logger.error(errorMessage);
            throw new DirectorySyncException(errorMessage);
        }
        return client.getAccessToken().getTokenValue();
    }
}
