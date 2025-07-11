package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final EmailService emailService;
    private final EmployeeRepository employeeRepository;

    public NotificationService(EmailService emailService, EmployeeRepository employeeRepository) {
        this.emailService = emailService;
        this.employeeRepository = employeeRepository;
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

        Email email = new Email(
                "noreply@company.com",
                List.of(visitor.getEmail()),
                subject,
                body,
                List.of()
        );

        emailService.sendEmail(email);
    }

    public void sendHostNotification(Visitor visitor, Long hostId) {
        employeeRepository.findById(hostId).ifPresent(host -> {
            String subject = "Visitor Alert";
            String body = String.format("""
                    Dear %s,
                    You have a new visitor coming to meet you.

                    Visitor Name: %s
                    Email: %s

                    Please be ready to receive them.
                    """, host.getName(), visitor.getName(), visitor.getEmail());


            Email email = new Email(
                    "noreply@company.com",
                    List.of(host.getEmail()),
                    subject,
                    body,
                    List.of()
            );

            emailService.sendEmail(email);
        });
    }
}
