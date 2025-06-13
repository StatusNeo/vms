package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile; // Add this import
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.statusneo.vms.repository.EmailService; 
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64; // Add this import for attachments

/**
 * Service responsible for handling all email communications in the Visitor Management System
 * using Microsoft Graph API for production environment.
 */
@Service
@Profile("!qa") // This service will be active for any profile EXCEPT "qa"
public class ProdEmailService implements EmailService { // Implement the interface

    private static final Logger logger = LoggerFactory.getLogger(ProdEmailService.class);

    private final VisitorRepository visitorRepository;
    private final ExcelService excelService;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    @Value("${visitor.system.notification.email}")
    private String recipientEmail; // Admin email for notifications

    @Value("${visitor.system.notification.subject}")
    private String notificationSubject;

    // We're using userEmail for sending, so senderEmail from Spring Mail config isn't directly used for Graph API calls
    // @Value("${spring.mail.username}") // Remove or comment out if not used
    // private String senderEmail;

    @Value("${app.user-email}")
    private String userEmail; // The user (email address) from which Graph API sends emails

    @Value("${visitor.system.employee.notification.subject}")
    private String employeeNotificationSubject; // Subject for employee meeting notifications

    @Autowired
    public ProdEmailService(VisitorRepository visitorRepository,
                            ExcelService excelService,
                            OAuth2AuthorizedClientManager authorizedClientManager,
                            RestTemplate restTemplate) {
        this.visitorRepository = visitorRepository;
        this.excelService = excelService;
        this.authorizedClientManager = authorizedClientManager;
        this.restTemplate = restTemplate;
    }

    // Public method for token retrieval, although typically internal to this service
    public String getAccessToken() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("azure")
                .principal("principal") // Using a dummy principal for client_credentials flow
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            return authorizedClient.getAccessToken().getTokenValue();
        }
        logger.error("Failed to obtain access token for Graph API. Check Azure AD configuration.");
        throw new RuntimeException("Failed to obtain access token for Graph API. Check Azure AD configuration.");
    }

    @Override // Implementing the interface method
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            String accessToken = getAccessToken(); // This throws RuntimeException on failure, which is propagated

            // Graph API endpoint for sending mail from a specific user
            String endpoint = String.format("https://graph.microsoft.com/v1.0/users/%s/sendMail", userEmail);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Constructing the message payload as required by Microsoft Graph API
            Map<String, Object> emailBody = new HashMap<>();
            emailBody.put("contentType", "Text"); // Can be HTML as well
            emailBody.put("content", body);

            Map<String, Object> message = new HashMap<>();
            message.put("subject", subject);
            message.put("body", emailBody);
            message.put("toRecipients", Collections.singletonList(
                    Collections.singletonMap("emailAddress",
                            Collections.singletonMap("address", toEmail))));

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("message", message);
            emailData.put("saveToSentItems", "true"); // Save a copy in the sender's Sent Items folder

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

            ResponseEntity<Void> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, Void.class);
            if (response.getStatusCode().equals(HttpStatus.ACCEPTED)) { // Graph API returns 202 Accepted for successful send
                logger.info("Email sent successfully to {} with subject: {}", toEmail, subject);
            } else {
                logger.error("Failed to send email to {} with subject {}. Graph API returned status: {}", toEmail, subject, response.getStatusCode());
                throw new RuntimeException("Failed to send email via Microsoft Graph API. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error sending email to {} with subject {}: {}", toEmail, subject, e.getMessage());
            throw new RuntimeException("Failed to send email via Microsoft Graph API to " + toEmail + ": " + e.getMessage(), e);
        }
    }

    @Override // Implementing the interface method
    public void sendVisitorEmail(Visitor visitor) {
        String subject = notificationSubject + ": " + visitor.getName();
        String body = createVisitorDetailsMessage(visitor);
        // Use the sendEmail method which now uses Graph API
        sendEmail(recipientEmail, subject, body); // This will propagate RuntimeException if send fails
    }

    /**
     * Creates a formatted message containing visitor details.
     *
     * @param visitor The visitor whose details to format.
     * @return A formatted string containing visitor information.
     */
    private String createVisitorDetailsMessage(Visitor visitor) {
        return String.format("""
            Visitor Details:
            Name: %s
            Phone: %s
            Email: %s
            Address: %s
            """,
            visitor.getName(),
            visitor.getPhoneNumber(),
            visitor.getEmail(),
            visitor.getAddress());
    }

    @Override // Implementing the interface method
    public void sendVisitorReport() {
        try {
            List<Visitor> visitors = visitorRepository.findAll();
            byte[] excelFile = excelService.generateVisitorExcel(visitors);
            String fileName = "Visitor_Report.xlsx";
            String subject = notificationSubject + " Report";
            String body = "Please find attached the visitor report.";

            // Instead of MimeMessageHelper, we'll build the Graph API request for sending email with attachment
            String accessToken = getAccessToken();
            String endpoint = String.format("https://graph.microsoft.com/v1.0/users/%s/sendMail", userEmail);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create attachment payload
            Map<String, Object> fileAttachment = new HashMap<>();
            fileAttachment.put("@odata.type", "#microsoft.graph.fileAttachment");
            fileAttachment.put("name", fileName);
            fileAttachment.put("contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            fileAttachment.put("contentBytes", Base64.getEncoder().encodeToString(excelFile)); // Base64 encode the file content
            fileAttachment.put("isInline", false); // Not an inline attachment (like an image in the body)

            // Create message payload
            Map<String, Object> emailBody = new HashMap<>();
            emailBody.put("contentType", "Text");
            emailBody.put("content", body);

            Map<String, Object> message = new HashMap<>();
            message.put("subject", subject);
            message.put("body", emailBody);
            message.put("toRecipients", Collections.singletonList(
                    Collections.singletonMap("emailAddress",
                            Collections.singletonMap("address", recipientEmail)))); // Send report to admin
            message.put("attachments", Collections.singletonList(fileAttachment));

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("message", message);
            emailData.put("saveToSentItems", "true");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

            ResponseEntity<Void> response = restTemplate.exchange(endpoint, HttpMethod.POST, request, Void.class);

            if (response.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                logger.info("Visitor report sent successfully to {} with subject: {}", recipientEmail, subject);
            } else {
                logger.error("Failed to send visitor report to {} with subject {}. Graph API returned status: {}", recipientEmail, subject, response.getStatusCode());
                throw new RuntimeException("Failed to send visitor report email via Microsoft Graph API. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error sending visitor report: {}", e.getMessage());
            throw new RuntimeException("Failed to send visitor report via Microsoft Graph API", e);
        }
    }

    @Override // Implementing the interface method
    public void sendOtp(String email, String otp) {
        sendEmail(email, "Your OTP", "Your OTP is: " + otp);
    }

    @Override // Implementing the interface method
    public void sendMeetingNotification(String email, String whomToMeet) {
        sendEmail(email, employeeNotificationSubject, "You have a visitor to meet: " + whomToMeet);
    }

    @Override
    public List<String> getSentEmails() {
        // This method is not applicable for the production service.
        // It's meant for the QA mock.
        return Collections.emptyList(); // Or throw UnsupportedOperationException
    }

    @Override
    public void clearSentEmails() {
        // This method is not applicable for the production service.
        // It's meant for the QA mock.
        // Do nothing
    }
}