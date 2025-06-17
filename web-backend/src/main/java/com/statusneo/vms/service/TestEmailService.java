package com.statusneo.vms.service;

import com.statusneo.vms.model.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile; // Add this import
import org.springframework.stereotype.Service;
import com.statusneo.vms.repository.EmailService;
import com.statusneo.vms.repository.OtpNotificationService;
import com.statusneo.vms.repository.VisitorNotificationService;
import com.statusneo.vms.repository.MeetingNotificationService;
import com.statusneo.vms.repository.VisitorReportService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock Email Service for QA environment.
 * This service does not send actual emails and logs attempts instead.
 */
@Service
@Profile("test") // This service will ONLY be active when the "test" profile is active
public class TestEmailService implements
                            EmailService,
                            OtpNotificationService,
                            VisitorNotificationService,
                            MeetingNotificationService,
                            VisitorReportService
{

    private static final Logger logger = LoggerFactory.getLogger(TestEmailService.class);

    private final List<String> sentEmailsLog = Collections.synchronizedList(new ArrayList<>());

    @Override 
    public void sendEmail(String toEmail, String subject, String body) {
        String logMessage = String.format(
                "MOCK EMAIL SENT (QA Profile) - To: %s, Subject: %s, Body: %s",
                toEmail, subject, body.replace("\n", " ").substring(0, Math.min(body.length(), 100)) + "...");
        logger.info(logMessage);
        sentEmailsLog.add(logMessage);
    }

    @Override
    public void sendVisitorEmail(Visitor visitor) {
        String body = String.format("MOCK Visitor Notification - Name: %s, Email: %s", visitor.getName(), visitor.getEmail());
        sendEmail("admin@example.com", "MOCK New Visitor Alert", body);
    }

    @Override
    public void sendVisitorReport() {
        String logMessage = "MOCK Visitor Report Sent (Attachment simulated)";
        logger.info(logMessage);
        sentEmailsLog.add(logMessage);
    }

    @Override
    public void sendOtp(String email, String otp) {
        String body = "MOCK Your OTP is: " + otp;
        sendEmail(email, "MOCK OTP for VMS", body);
    }

    @Override
    public void sendMeetingNotification(String email, String whomToMeet) {
        String body = "MOCK You have a visitor to meet: " + whomToMeet;
        sendEmail(email, "MOCK Meeting Notification", body);
    }
}