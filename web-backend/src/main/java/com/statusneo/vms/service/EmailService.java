package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Map;

/**
 * Service responsible for handling all email communications in the Visitor Management System.
 * Manages visitor notifications, OTP delivery, and employee communications.
 */
@Service
public class EmailService {
    private final VisitorRepository visitorRepository;
    private final ExcelService excelService;

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

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
     * @param visitorRepository Repository for visitor data access
     * @param excelService      Service for generating Excel reports
     */
    @Autowired
    public EmailService(VisitorRepository visitorRepository,
                        ExcelService excelService,
                        OAuth2AuthorizedClientManager authorizedClientManager,
                        RestTemplate restTemplate) {
        this.visitorRepository = visitorRepository;
        this.excelService = excelService;
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
        String accessToken = getAccessToken();
        String endpoint = String.format("https://graph.microsoft.com/v1.0/users/%s/sendMail", userEmail);

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
     * Sends a notification email about a new visitor to the system administrator.
     *
     * @param visitor The visitor whose details should be included in the notification
     * @throws RuntimeException if email sending fails
     */
//    public void sendVisitorEmail(Visitor visitor) {
//        try {
//
//            String body = createVisitorDetailsMessage(visitor);
//            sendEmail(visitor.getEmail(), "Visitor Registration Confirmation", body);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to send visitor email", e);
//        }
//    }

    public String getEmailFromAzureAD(String displayName) {
        logger.info("Attempting to fetch email for display name: {}", displayName);
        String accessToken = getAccessToken();
        logger.debug("****************************Access token retrieved successfully.*********************************8");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);

        String url = "https://graph.microsoft.com/v1.0/users?$filter=displayName eq '" + displayName + "'";

        logger.debug("***************************Sending request to URL: {}*******************************", url);


        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody().get("value");

        if (!users.isEmpty()) {
            return (String) users.get(0).get("mail");
        }

        logger.error("**********************No email found for display name: {}*********************", displayName);

        throw new RuntimeException("***********************Host email not found in Azure AD for name: **********************" + displayName);
    }


    /**
     * Creates a formatted message containing visitor details.
     *
     * @param visitor The visitor whose details to format
     * @return A formatted string containing visitor information
     */
//    private String createVisitorDetailsMessage(Visitor visitor) {
//        return String.format("""
//            Visitor Details:
//            Name: %s
//            Phone: %s
//            Email: %s
//            Address: %s
//            """,
//                visitor.getName(),
//                visitor.getPhoneNumber(),
//                visitor.getEmail(),
//                visitor.getAddress());
//    }


    public void sendVisitorReport() {
        try {
            List<Visitor> visitors = visitorRepository.findAll();
            byte[] excelFile = excelService.generateVisitorExcel(visitors);

//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setFrom(senderEmail);
//            helper.setTo(recipientEmail);
//            helper.setSubject(notificationSubject + " Report");
//            helper.setText("Please find attached the visitor report.");
//            helper.addAttachment("Visitor_Report.xlsx", new ByteArrayResource(excelFile));

        } catch (Exception e) {
            throw new RuntimeException("Failed to send visitor report", e);
        }
    }

    // Add methods from NotificationService
    public void sendOtp(String email, String otp) {
        sendEmail(email, "Your OTP", "Your OTP is: " + otp);
    }

    public void sendMeetingNotification(String email, String whomToMeet) {
        sendEmail(email, "Meeting Notification", "You have a visitor to meet: " + whomToMeet);
    }

}
