package com.statusneo.vms.service;

import com.fasterxml.jackson.databind.JsonNode;
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


    public GraphApiService(RestTemplate restTemplate, OAuth2AuthorizedClientManager authorizedClientManager) {
        this.restTemplate = restTemplate;
        this.authorizedClientManager = authorizedClientManager;
    }

    /**
     * Gets an access token for Microsoft Graph API.
     *
     * @return The access token
     * @throws RuntimeException if token retrieval fails
     */
    public String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("azure")
                .principal("principal")  // using a string principal for client_credentials flow
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null) {
            return authorizedClient.getAccessToken().getTokenValue();
        }

        throw new RuntimeException("Failed to obtain access token");
    }

    public List<String> getEmployees() {
        HttpHeaders headers = new HttpHeaders();
        String accessToken = getAccessToken();

        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            graphApiUrl + "/users", HttpMethod.GET, request, JsonNode.class);

        List<String> employeeNames = new ArrayList<>();
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            for (JsonNode user : response.getBody().get("value")) {
                employeeNames.add(user.get("displayName").asText());
            }
        }

        return employeeNames;
    }
}
