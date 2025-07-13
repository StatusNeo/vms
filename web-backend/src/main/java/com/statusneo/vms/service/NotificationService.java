package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
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
        String body = String.format("""
                Dear %s,
                You have a new visitor coming to meet you.

                Visitor Name: %s
                Email: %s

                Please be ready to receive them.
                """, host.getName(), visitor.getName(), visitor.getEmail());

        Email email = Email.of(
                "noreply@company.com",
                List.of(host.getEmail()),
                subject,
                body
        );

        emailService.sendEmail(email);
    }
}
