package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Profile({"default", "dev", "test"})
public class WiremockMailServiceImpl implements EmailService{

    private final RestTemplate restTemplate;

    @Value("${wiremock.mail.url}")
    private String wiremockMailUrl;

    public WiremockMailServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    /**
     * Sends a simple email for local/dev testing.
     *
     * */
    public boolean sendEmail(Email email) {
        String endpoint = String.format(wiremockMailUrl, email.from());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setBearerAuth("dummy-token");
        Map<String, Object> payload = Map.of(
                "message", Map.of(
                        "subject", email.subject(),
                        "body", Map.of(
                                "contentType", "Text",
                                "content", email.body()
                        ),
                        "toRecipients", List.of(
                                Map.of("emailAddress", Map.of("address", email.to()))
                        )
                ),
                "saveToSentItems", true
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
        return response.getStatusCode().equals(HttpStatus.ACCEPTED);
    }
}
