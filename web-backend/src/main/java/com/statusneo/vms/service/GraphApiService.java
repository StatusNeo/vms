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

import com.fasterxml.jackson.databind.JsonNode;
import com.statusneo.vms.model.EmployeeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphApiService {

    @Value("${graph.api.url}")
    private String graphApiUrl;

    private final RestTemplate restTemplate;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final AccessTokenService accessTokenService;


    public GraphApiService(RestTemplate restTemplate, OAuth2AuthorizedClientManager authorizedClientManager, AccessTokenService accessTokenService) {
        this.restTemplate = restTemplate;
        this.authorizedClientManager = authorizedClientManager;
        this.accessTokenService = accessTokenService;
    }


    public List<EmployeeDTO> getEmployees() {
        HttpHeaders headers = new HttpHeaders();
        String accessToken = accessTokenService.getAccessToken();

        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            graphApiUrl + "/users", HttpMethod.GET, request, JsonNode.class);

        List<EmployeeDTO> employees = new ArrayList<>();
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            for (JsonNode user : response.getBody().get("value")) {
                String displayName = user.hasNonNull("displayName") ? user.get("displayName").asText() : "N/A";
                String mail = user.hasNonNull("mail") ? user.get("mail").asText() : "N/A";
                employees.add(new EmployeeDTO(displayName, mail));
            }
        }

        return employees;
    }
}
