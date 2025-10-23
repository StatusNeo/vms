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

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphDirectoryService {

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
     * It returns the total number of upserts performed.
     *
     * @return total number of upserts performed
     */
    public int syncAllUsersToEmployees() {
        String token = getAccessToken();
        String url = "https://graph.microsoft.com/v1.0/users?$select=displayName,mail,userPrincipalName&$top=9";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String nextLink = url;
        List<String> namesForCache = new ArrayList<>();
        int processed = 0;

        while (nextLink != null) {
            // Log the outgoing HTTP request (method + url) while redacting sensitive headers
            logger.info("Graph API request: method=GET url={} headers={}", nextLink, maskHeaders(headers));
            ResponseEntity<Map> response = restClient.get()
                    .uri(nextLink)
                    .headers(httpHeaders -> {
                        httpHeaders.setBearerAuth(token);
                        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    })
                    .retrieve()
                    .toEntity(Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                break;
            }
            Map<String, Object> body = response.getBody();
            if (body == null) {
                break;
            }
            Object values = body.get("value");
            if (values instanceof List<?> list) {
                // Collect emails and display names from the page
                List<Map<String, Object>> userMaps = list.stream()
                        .filter(item -> item instanceof Map<?, ?>)
                        .map(item -> (Map<String, Object>) item)
                        .collect(Collectors.toList());

                Set<String> emailsInPage = userMaps.stream()
                        .map(m -> {
                            String mail = Objects.toString(m.get("mail"), null);
                            String upn = Objects.toString(m.get("userPrincipalName"), null);
                            String email = mail != null && !mail.isBlank() ? mail : upn;
                            return (email == null || email.isBlank()) ? null : email.toLowerCase();
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                if (!emailsInPage.isEmpty()) {
                    // Load existing employees for the page in one query
                    List<Employee> existing = employeeRepository.findByEmailIn(emailsInPage);
                    Set<String> existingEmails = existing.stream()
                            .map(e -> e.getEmail().toLowerCase())
                            .collect(Collectors.toSet());

                    List<Employee> toSave = new ArrayList<>();

                    // For each user map, decide whether to insert new or update existing entity
                    for (Map<String, Object> userMap : userMaps) {
                        String displayName = Objects.toString(userMap.get("displayName"), null);
                        String mail = Objects.toString(userMap.get("mail"), null);
                        String upn = Objects.toString(userMap.get("userPrincipalName"), null);
                        String email = mail != null && !mail.isBlank() ? mail : upn;
                        if (email == null || email.isBlank() || displayName == null || displayName.isBlank()) {
                            continue;
                        }
                        String emailLower = email.toLowerCase();
                        if (existingEmails.contains(emailLower)) {
                            // update the existing entity's name
                            for (Employee e : existing) {
                                if (e.getEmail().equalsIgnoreCase(email)) {
                                    if (!displayName.equals(e.getName())) {
                                        e.setName(displayName);
                                        toSave.add(e);
                                    }
                                    break;
                                }
                            }
                        } else {
                            Employee e = new Employee();
                            e.setEmail(email);
                            e.setName(displayName);
                            toSave.add(e);
                        }
                    }

                    if (!toSave.isEmpty()) {
                        List<Employee> saved = employeeRepository.saveAll(toSave);
                        // Update cache and processed count
                        for (Employee s : saved) {
                            namesForCache.add(s.getName());
                        }
                        processed += saved.size();
                    }
                }
            }

            Object next = body.get("@odata.nextLink");
            nextLink = next instanceof String ? (String) next : null;
        }

        employeeNameCache.clear();
        employeeNameCache.bulkInsert(namesForCache);
        int upserts = processed;
        logger.info("Employee sync complete. Upserts: {}", upserts);
        return upserts;
    }

    // Return a string representation of headers with Authorization redacted for safe logging
    private String maskHeaders(HttpHeaders headers) {
        if (headers == null) return "{}";
        HttpHeaders copy = new HttpHeaders();
        headers.forEach((k, v) -> {
            if ("Authorization".equalsIgnoreCase(k)) {
                copy.put(k, Collections.singletonList("REDACTED"));
            } else {
                copy.put(k, v);
            }
        });
        return copy.toString();
    }

    private String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("azure")
                .principal("principal")
                .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("Failed to obtain Graph access token");
        }
        return client.getAccessToken().getTokenValue();
    }
}
