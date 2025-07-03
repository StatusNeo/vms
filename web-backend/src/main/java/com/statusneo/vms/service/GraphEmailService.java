package com.statusneo.vms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Profile("prod")
public class GraphEmailService implements MailService{

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    private final RestTemplate restTemplate;

    public GraphEmailService(OAuth2AuthorizedClientManager authorizedClientManager, RestTemplate restTemplate) {
        this.authorizedClientManager = authorizedClientManager;
        this.restTemplate = restTemplate;
    }

    public String sendEmail(String toEmail, String subject, String content) {
        // Get access token
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("azure")
                .principal("graph-client")
                .build();

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        OAuth2AccessToken accessToken = client.getAccessToken();

        String url = "https://graph.microsoft.com/v1.0/users/YOUR_SENDER_ID/sendMail";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
            {
              "message": {
                "subject": "%s",
                "body": {
                  "contentType": "Text",
                  "content": "%s"
                },
                "toRecipients": [
                  {
                    "emailAddress": {
                      "address": "%s"
                    }
                  }
                ]
              },
              "saveToSentItems": "true"
            }
            """.formatted(subject, content, toEmail);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return response.getStatusCode().is2xxSuccessful() ? "Email sent!" : "Failed: " + response.getBody();
    }
}
