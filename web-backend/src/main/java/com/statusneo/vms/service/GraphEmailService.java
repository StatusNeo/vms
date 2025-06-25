package com.statusneo.vms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service for sending emails using Microsoft Graph API.
 * Handles different types of emails including plain text emails and emails with attachments.
 */
@Service
public class GraphEmailService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    @Value("${app.user-email}")
    private String userEmail;

    /**
     * Constructs a new GraphEmailService with required dependencies.
     *
     * @param authorizedClientManager Client manager for OAuth2 authentication
     * @param restTemplate            RestTemplate for making API calls
     */
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
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param body    Email body content
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        Map<String, Object> emailData = createEmailData(userEmail, toEmail, subject, body, null);
        return sendEmailWithHeaders(emailData, userEmail);
    }

    /**
     * Sends a simple email with a specified sender.
     *
     * @param fromEmail Sender email address
     * @param toEmail   Recipient email address
     * @param subject   Email subject
     * @param body      Email body content
     * @return true if the email was sent successfully, false otherwise
     */
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

    /**
     * Helper method to create a structured email data map.
     */
    private Map<String, Object> createEmailData(
            String fromEmail,
            String toEmail,
            String subject,
            String body,
            List<Map<String, Object>> attachments) {

        Map<String, Object> emailData = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        Map<String, Object> emailBody = new HashMap<>();

        emailBody.put("contentType", "Text");
        emailBody.put("content", body);

        message.put("subject", subject);
        message.put("body", emailBody);

        Map<String, Object> fromEmailObj = new HashMap<>();
        fromEmailObj.put("address", fromEmail);

        Map<String, Object> fromRecipient = new HashMap<>();
        fromRecipient.put("emailAddress", fromEmailObj);

        List<Map<String, Object>> fromRecipients = new ArrayList<>();
        fromRecipients.add(fromRecipient);

        message.put("from", fromRecipients);

        Map<String, Object> toEmailObj = new HashMap<>();
        toEmailObj.put("address", toEmail);

        Map<String, Object> toRecipient = new HashMap<>();
        toRecipient.put("emailAddress", toEmailObj);

        List<Map<String, Object>> toRecipients = new ArrayList<>();
        toRecipients.add(toRecipient);

        message.put("toRecipients", toRecipients);

        if (attachments != null && !attachments.isEmpty()) {
            message.put("attachments", attachments);
        }

        emailData.put("message", message);
        emailData.put("saveToSentItems", "true");

        return emailData;
    }

    /**
     * Sends an email using the provided email data and sender email address.
     *
     * @param emailData   Email data to be sent
     * @param fromEmail   Sender email address
     * @return true if the email was sent successfully, false otherwise
     */
    private boolean sendEmailWithHeaders(Map<String, Object> emailData, String fromEmail) {
        String accessToken = getAccessToken();
        String endpoint = String.format("https://graph.microsoft.com/v1.0/users/%s/sendMail", fromEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);
        ResponseEntity<Void> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, Void.class);

        return response.getStatusCode().equals(HttpStatus.ACCEPTED);
    }

    /**
     * Creates a structured email for API calls and returns its byte representation.
     *
     * @param fromEmail Sender email address
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param body Email body content
     * @return Byte array representation of the email
     */
    public byte[] sendEmail2(String fromEmail, String toEmail, String subject, String body) {
        // Create email JSON structure and serialize to bytes
        Map<String, Object> emailData = createEmailData(fromEmail, toEmail, subject, body, null);
        return emailDataToBytes(emailData);
    }


    /**
     * Sends an email using its byte representation.
     *
     * @param emailBytes Byte array representation of the email
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendEmail(byte[] emailBytes) {
        Map<String, Object> emailData = bytesToEmailData(emailBytes);

        // Extract sender email address
        Map<String, Object> message = (Map<String, Object>) emailData.get("message");
        List<Map<String, Object>> fromRecipients = (List<Map<String, Object>>) message.get("from");
        Map<String, Object> fromEmailAddress = (Map<String, Object>) fromRecipients.get(0).get("emailAddress");
        String fromEmail = (String) fromEmailAddress.get("address");

        return sendEmailWithHeaders(emailData, fromEmail);
    }

    /**
     * Creates an email with a file attachment and returns its byte representation.
     *
     * @param fromEmail      Sender email address
     * @param toEmail        Recipient email address
     * @param subject        Email subject
     * @param body           Email body content
     * @param attachmentName Name of the file attachment
     * @param attachment     File content as ByteArrayResource
     * @return Byte array representation of the email with attachment
     */
    public byte[] createReportEmail(
            String fromEmail,
            String toEmail,
            String subject,
            String body,
            String attachmentName,
            ByteArrayResource attachment) {

        Map<String, Object> attachmentData = new HashMap<>();
        attachmentData.put("@odata.type", "#microsoft.graph.fileAttachment");
        attachmentData.put("name", attachmentName);
        attachmentData.put("contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        attachmentData.put("contentBytes", java.util.Base64.getEncoder().encodeToString(attachment.getByteArray()));

        Map<String, Object> emailData = createEmailData(fromEmail, toEmail, subject, body, Collections.singletonList(attachmentData));
        return emailDataToBytes(emailData);
    }

    /**
     * Helper method to convert email data to byte array.
     */
    private byte[] emailDataToBytes(Map<String, Object> emailData) {
        // This is a simplification - in a real application you would use JSON serialization
        return emailData.toString().getBytes();
    }

    /**
     * Helper method to convert byte array back to email data.
     */
    private Map<String, Object> bytesToEmailData(byte[] emailBytes) {
        // This is a simplification - in a real application you would use JSON deserialization
        @SuppressWarnings("unchecked")
        Map<String, Object> result = new HashMap<>(); // In reality, deserialize JSON bytes

        // Placeholder implementation - in a real app, this would be actual deserialization
        String jsonString = new String(emailBytes);
        // Parse jsonString into result map

        return result;
    }

    /**
     * Creates an email with the given parameters and returns its byte representation.
     *
     * @param fromEmail      Sender email address
     * @param toEmail        Recipient email address
     * @param subject        Email subject
     * @param body           Email body content
     * @param attachmentName Name of the file attachment (null if no attachment)
     * @param attachment     File content as ByteArrayResource (null if no attachment)
     * @return Byte array representation of the email
     */
    public byte[] createAndSendEmail(String fromEmail, String toEmail, String subject, String body, String attachmentName, ByteArrayResource attachment) {
        Map<String, Object> emailData = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        Map<String, Object> emailBody = new HashMap<>();

        emailBody.put("contentType", "Text");
        emailBody.put("content", body);

        message.put("subject", subject);
        message.put("body", emailBody);

        Map<String, Object> fromEmailObj = new HashMap<>();
        fromEmailObj.put("address", fromEmail);

        Map<String, Object> fromRecipient = new HashMap<>();
        fromRecipient.put("emailAddress", fromEmailObj);

        List<Map<String, Object>> fromRecipients = new ArrayList<>();
        fromRecipients.add(fromRecipient);

        message.put("from", fromRecipients);

        Map<String, Object> toEmailObj = new HashMap<>();
        toEmailObj.put("address", toEmail);

        Map<String, Object> toRecipient = new HashMap<>();
        toRecipient.put("emailAddress", toEmailObj);

        List<Map<String, Object>> toRecipients = new ArrayList<>();
        toRecipients.add(toRecipient);

        message.put("toRecipients", toRecipients);

        if (attachmentName != null && attachment != null) {
            Map<String, Object> attachmentData = new HashMap<>();
            attachmentData.put("@odata.type", "#microsoft.graph.fileAttachment");
            attachmentData.put("name", attachmentName);
            attachmentData.put("contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            attachmentData.put("contentBytes", java.util.Base64.getEncoder().encodeToString(attachment.getByteArray()));

            message.put("attachments", Collections.singletonList(attachmentData));
        }

        emailData.put("message", message);
        emailData.put("saveToSentItems", "true");

        return emailDataToBytes(emailData);
    }


}

