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

import com.statusneo.vms.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for handling all email communications in the Visitor Management System.
 */
@Service
@Profile({"prod", "default", "sqlite", "test"})
public class GraphEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(GraphEmailService.class);

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public GraphEmailService(OAuth2AuthorizedClientManager authorizedClientManager,
                             RestClient restClient) {
        this.authorizedClientManager = authorizedClientManager;
        this.restClient = restClient;
    }

    public String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("azure")
                .principal("principal")
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient != null) {
            return authorizedClient.getAccessToken().getTokenValue();
        }
        throw new RuntimeException("Failed to obtain access token");
    }

    @Override
    public boolean sendEmail(Email email) {
        String accessToken = getAccessToken();
        String endpointUsers = String.format("%s/users/%s/sendMail", graphApiBaseUrl, email.from());

        Map<String, Object> emailData = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        Map<String, Object> emailBody = new HashMap<>();

        emailBody.put("contentType", "Text");
        emailBody.put("content", email.body());

        message.put("subject", email.subject());
        message.put("body", emailBody);
        message.put("toRecipients", email.to().stream()
                .map(addr -> Collections.singletonMap("emailAddress", Collections.singletonMap("address", addr)))
                .toList());

        if (email.attachments() != null && !email.attachments().isEmpty()) {
            var attachmentsList = email.attachments().stream().map(att -> {
                Map<String, Object> attMap = new HashMap<>();
                attMap.put("@odata.type", "#microsoft.graph.fileAttachment");
                attMap.put("name", att.filename());
                attMap.put("contentType", att.contentType());
                attMap.put("contentBytes", Base64.getEncoder().encodeToString(att.data()));
                return attMap;
            }).toList();
            message.put("attachments", attachmentsList);
        }

        emailData.put("message", message);
        emailData.put("saveToSentItems", "true");

        // Do not swallow HTTP errors (especially 404 for invalid system sender). Let them propagate so the caller
        // (VisitService / controller) can handle them as system errors.
        ResponseEntity<Void> response = restClient.post()
                .uri(endpointUsers)
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(accessToken);
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .body(emailData)
                .retrieve()
                .toBodilessEntity();

        boolean accepted = response.getStatusCode().equals(HttpStatus.ACCEPTED);
        if (!accepted) {
            logger.warn("Graph Email API returned {} for sender {} to {}", response.getStatusCode(), email.from(), email.to());
        }
        return accepted;
    }
}
