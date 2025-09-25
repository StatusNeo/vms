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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.repository.EmployeeRepository;

@Service
public class GraphDirectoryService {

    private static final Logger logger = LoggerFactory.getLogger(GraphDirectoryService.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;
    private final EmployeeRepository employeeRepository;
    private final EmployeeNameCache employeeNameCache;

    public GraphDirectoryService(OAuth2AuthorizedClientManager authorizedClientManager,
                                 RestTemplate restTemplate,
                                 EmployeeRepository employeeRepository,
                                 EmployeeNameCache employeeNameCache) {
        this.authorizedClientManager = authorizedClientManager;
        this.restTemplate = restTemplate;
        this.employeeRepository = employeeRepository;
        this.employeeNameCache = employeeNameCache;
    }

    public int syncAllUsersToEmployees() {
        String token = getAccessToken();
//      String url = "https://graph.microsoft.com/v1.0/users?$select=displayName,mail,userPrincipalName&$top=999";
        String url = "";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        int upserts = 0;
        String nextLink = url;
        List<String> namesForCache = new ArrayList<>();
        while (nextLink != null) {

            ResponseEntity<Map> response = restTemplate.exchange(nextLink, HttpMethod.GET, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                break;
            }
            Map<String, Object> body = response.getBody();
            if (body == null) {
                break;
            }
            Object values = body.get("value");
            if (values instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Map<?, ?> userMap) {
                        String displayName = Objects.toString(userMap.get("displayName"), null);
                        String mail = Objects.toString(userMap.get("mail"), null);
                        String upn = Objects.toString(userMap.get("userPrincipalName"), null);
                        String email = mail != null && !mail.isBlank() ? mail : upn;
                        if (email == null || email.isBlank() || displayName == null || displayName.isBlank()) {
                            continue;
                        }
                        Employee toSave = employeeRepository.findByEmail(email)
                                .map(existing -> {
                                    existing.setName(displayName);
                                    return existing;
                                })
                                .orElseGet(() -> {
                                    Employee e = new Employee();
                                    e.setEmail(email);
                                    e.setName(displayName);
                                    return e;
                                });
                        Employee saved = employeeRepository.save(toSave);
                        namesForCache.add(saved.getName());
                        upserts++;
                    }
                }
            }
            Object next = body.get("@odata.nextLink");
            nextLink = next instanceof String ? (String) next : null;
        }

        employeeNameCache.clear();
        employeeNameCache.bulkInsert(namesForCache);
        logger.info("Employee sync complete. Upserts: {}", upserts);
        return upserts;
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


