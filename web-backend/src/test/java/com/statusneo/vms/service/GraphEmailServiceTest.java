package com.statusneo.vms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GraphEmailServiceTest {

    @Mock
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private OAuth2AccessToken accessToken;

    @InjectMocks
    private GraphEmailService graphEmailService;

   @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        graphEmailService = new GraphEmailService(authorizedClientManager, restTemplate);
    }

    @Test
    void testSendEmail_Success() {
        // Arrange
        String fromEmail = "system-user@example.com";
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        String fakeToken = "fake-access-token";

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(fakeToken);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = graphEmailService.sendEmail(fromEmail, toEmail, subject, body);

        // Assert
        assertTrue(result);
        verify(restTemplate, times(1)).exchange(contains("graph.microsoft.com"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    void testSendEmail_Failure() {
        // Arrange
        String fromEmail = "system-user@example.com";
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        String fakeToken = "fake-access-token";

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(fakeToken);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = graphEmailService.sendEmail(fromEmail, toEmail, subject, body );

        // Assert
        assertFalse(result);
        verify(restTemplate, times(1)).exchange(contains("graph.microsoft.com"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }
} 