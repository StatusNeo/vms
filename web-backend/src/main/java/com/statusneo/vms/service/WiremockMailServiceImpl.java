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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.statusneo.vms.model.Email;

@Service
@Profile({"dev", "test", "sqlite"})
public class WiremockMailServiceImpl implements EmailService {

    private final RestClient restClient;

    @Value("${mail.url}")
    private String wiremockMailUrl;

    public WiremockMailServiceImpl(RestClient restClient) {
        this.restClient = restClient;
    }
    /**
     * Sends a simple email for local/dev testing.
     */
    @Override
    public boolean sendEmail(Email email) {
        String template = Objects.requireNonNull(wiremockMailUrl, "mail.url must be configured");
        String endpoint = String.format(template, email.from());
        List<Map<String, Object>> recipients = email.to().stream()
                .map(addr -> {
                    Map<String, Object> address = new HashMap<>();
                    address.put("address", addr);

                    Map<String, Object> recipient = new HashMap<>();
                    recipient.put("emailAddress", address);
                    return recipient;
                })
                .toList();

        Map<String, Object> message = Map.of(
                "subject", email.subject(),
                "body", Map.of(
                        "contentType", "Text",
                        "content", email.body()
                ),
                "toRecipients", recipients
        );

        Map<String, Object> payload = Map.of(
                "message", message,
                "saveToSentItems", true
        );

        ResponseEntity<Void> response = restClient.post()
                .uri(Objects.requireNonNull(endpoint, "endpoint must not be null"))
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth("dummy-token");
                })
                .body(Objects.requireNonNull(payload, "payload must not be null"))
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().equals(HttpStatus.ACCEPTED);
    }
}
