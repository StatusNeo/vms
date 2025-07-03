package com.statusneo.vms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for handling all email communications in the Visitor Management System.
 * Manages visitor notifications, OTP delivery, and employee communications.
 */
@Service
public class GraphEmailService implements EmailService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    @Value("${visitor.system.notification.email}")
    private String recipientEmail;

    @Value("${visitor.system.notification.subject}")
    private String notificationSubject;

    private String senderEmail;

    @Value("${app.user-email}")
    private String userEmail;

    @Value("${visitor.system.employee.notification.subject}")
    private String employeeNotificationSubject;

    /**
     * Constructs a new EmailService with required dependencies.
     *
     */
    @Autowired
    public GraphEmailService(OAuth2AuthorizedClientManager authorizedClientManager,
                             RestTemplate restTemplate) {
        this.authorizedClientManager = authorizedClientManager;
        this.restTemplate = restTemplate;
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

    /**
     * Sends a simple email with the default sender.
     *
     * @param toEmail   Recipient email address
     * @param subject   Email subject
     * @param body      Email body content
     * @param fromEmail
     * @return true if the email was sent successfully, false otherwise
     */
    @Override
    public boolean sendEmail(String fromEmail, String toEmail, String subject, String body) {
        String accessToken = getAccessToken();
        String endpoint = String.format("https://graph.microsoft.com/v1.0/users/%s/sendMail", fromEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, Object> emailData = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        Map<String, Object> emailBody = new HashMap<>();

        emailBody.put("contentType", "Text");
        emailBody.put("content", body);


        message.put("subject", subject);
        message.put("body", emailBody);
        message.put("toRecipients", Collections.singletonList(
                Collections.singletonMap("emailAddress",
                        Collections.singletonMap("address", toEmail))));

        emailData.put("message", message);
        emailData.put("saveToSentItems", "true");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);
        ResponseEntity<Void> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, Void.class);

        return response.getStatusCode().equals(HttpStatus.ACCEPTED);
    }



}
