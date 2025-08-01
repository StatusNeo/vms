package com.statusneo.vms.service;

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public AccessTokenService(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    public String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("azure")
                .principal("principal")  // client_credentials flow
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            return authorizedClient.getAccessToken().getTokenValue();
        }

        throw new RuntimeException("Failed to obtain access token");
    }
}
