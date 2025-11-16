package com.statusneo.vms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.statusneo.vms.model.Email;

import java.time.Instant;
import java.util.Map;


@ActiveProfiles("test")
class GraphEmailServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private GraphEmailService graphEmailService;

    @Mock
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "dummy-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(authorizedClientManager.authorize(any())).thenReturn(authorizedClient);
        ReflectionTestUtils.setField(graphEmailService, "graphApiBaseUrl", "https://graph.microsoft.com/v1.0");
    }

    @Test
    void testSendEmail_Success() {
        // Arrange
        String fromEmail = "system-user@example.com";
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.ACCEPTED);
        mockRestClientResponse(responseEntity);

        // Act
        boolean result = graphEmailService.sendEmail(Email.of(fromEmail, toEmail, subject, body));

        // Assert
        assertTrue(result);
        verify(requestBodyUriSpec).uri(contains("graph.microsoft.com"));
        verify(responseSpec).toBodilessEntity();
    }

    @Test
    void testSendEmail_Failure() {
        // Arrange
        String fromEmail = "system-user@example.com";
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        mockRestClientResponse(responseEntity);

        // Act
        boolean result = graphEmailService.sendEmail(Email.of(fromEmail, toEmail, subject, body));

        // Assert
        assertFalse(result);
        verify(requestBodyUriSpec).uri(contains("graph.microsoft.com"));
        verify(responseSpec).toBodilessEntity();
    }

    private void mockRestClientResponse(ResponseEntity<Void> responseEntity) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(responseEntity);
    }

    @Test
    void testSendEmail_AccessTokenFailure() {
        when(authorizedClientManager.authorize(any())).thenReturn(null);

        String fromEmail = "system-user@example.com";
        String toEmail = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                graphEmailService.sendEmail(Email.of(fromEmail, toEmail, subject, body))
        );

        assertEquals("Failed to obtain access token", exception.getMessage());
    }
}