package com.statusneo.vms.service;

import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${visitor.system.employee.notification.subject}")
    private String employeeSubject;

    private final EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void notifyVisitorOnRegistration(Visitor visitor) {
        logger.info("*******************Preparing visitor registration email for visitor: {}**************************", visitor.getEmail());
        String message = createVisitorMessage(visitor);
        boolean emailSent = emailService.sendEmail(visitor.getEmail(), "Visitor Registration Successful", message);
        if (emailSent) {
            logger.info("*************************8Registration email successfully sent to visitor: {} (ID: {})******************************", visitor.getEmail(), visitor.getId());
        } else {
            logger.warn("****************************8Failed to send registration email to visitor: {} (ID: {})***************************", visitor.getEmail(), visitor.getId());
        }
        System.out.println("Visitor ID: " + visitor.getId());
    }

    public void notifyHost(Visit visit) {
        logger.info("***********************Fetching host email from Azure AD for host name: {}*************************", visit.getHost());
        String hostName = visit.getHost();
        String hostEmail = emailService.getEmailFromAzureAD(hostName);

        String message = String.format("""
        You have a visitor scheduled to meet you.

        Visitor Name: %s
        Email: %s
        Visit Time: %s
        """,
                visit.getVisitor().getName(),
                visit.getVisitor().getEmail(),
                visit.getVisitDate()
        );

        emailService.sendEmail(hostEmail, employeeSubject, message);
    }



    private String createVisitorMessage(Visitor visitor) {
        logger.debug("*************************Creating email message for visitor: {}********************************", visitor.getEmail());
        return String.format("""
            Hello %s,

            Your registration was successful.

            Visitor Details:
            Name: %s
            Email: %s
            Address: %s

            Thank you.
            """,
                visitor.getName(),
                visitor.getEmail(),
                visitor.getAddress()
        );
    }
}
