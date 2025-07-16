package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.util.EmailTemplateProcessor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private final EmailService emailService;
    private final EmailTemplateProcessor templateProcessor;

    public NotificationService(EmailService emailService, EmailTemplateProcessor templateProcessor) {
        this.emailService = emailService;
        this.templateProcessor = templateProcessor;
    }

    public void sendVisitorConfirmationEmail(Visitor visitor) {
        String subject = "Registration Successful";
        String body = String.format("""
                Dear %s,
                Your visit registration was successful.

                Name: %s
                Email: %s

                Thank you!
                """, visitor.getName(), visitor.getName(), visitor.getEmail());

        Email email = Email.of(
                "noreply@company.com",
                List.of(visitor.getEmail()),
                subject,
                body
        );

        emailService.sendEmail(email);
    }

    public void sendHostNotification(Visitor visitor, Employee host) {
        String subject = "Visitor Alert";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("hostName", host.getName());
        placeholders.put("visitorName", visitor.getName());
        placeholders.put("visitorEmail", visitor.getEmail());

                  String body = templateProcessor.loadTemplate("hostNotification.txt", placeholders);

        Email email = Email.of(
                "noreply@company.com",
                List.of(host.getEmail()),
                subject,
                body
        );

        emailService.sendEmail(email);
    }
}
