package com.statusneo.vms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.statusneo.vms.model.Email;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(responseEntity);
    }
}