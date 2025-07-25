package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visitor;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    public NotificationService(EmailService emailService, TemplateEngine templateEngine) {
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    public void sendVisitorConfirmationEmail(Visitor visitor) {
        Map<String, Object> params = new HashMap<>();
        params.put("visitorName", visitor.getName());
        params.put("visitorEmail", visitor.getEmail());

        StringOutput output = new StringOutput();
        templateEngine.render("visitorConfirmation.jte", params, output);

        Email email = Email.of(
                "noreply@company.com",
                List.of(visitor.getEmail()),
                "Registration Successful",
                output.toString()
        );

        emailService.sendEmail(email);
    }

    public void sendHostNotification(Visitor visitor, Employee host) {
        Map<String, Object> params = new HashMap<>();
        params.put("hostName", host.getName());
        params.put("visitorName", visitor.getName());
        params.put("visitorEmail", visitor.getEmail());

        StringOutput output = new StringOutput();
        templateEngine.render("hostNotification.jte", params, output);

        Email email = Email.of(
                "noreply@company.com",
                List.of(host.getEmail()),
                "Visitor Alert",
                output.toString()
        );

        emailService.sendEmail(email);
    }
}