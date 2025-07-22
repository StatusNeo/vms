/* ********************************************************************
 *
 *  Copyright (c) 2025 Statusneo.
 *
 * All rights Reserved.
 *
 * Redistribution and use of any source or binary or in any form, without
 * written approval and permission is prohibited.
 *
 * Please read the Terms of use, Disclaimer and Privacy Policy on https://www.statusneo.com/
 *
 * **********************************************************************
 */

/*
 * Notification Service to send Email notification to host and visitor
 *
 *  @author Anurag Sharma
 *
 */

package com.statusneo.vms.service;

import com.statusneo.vms.config.EmailProperties;
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
    private final EmailProperties emailProperties;

    public NotificationService(EmailService emailService, EmailTemplateProcessor templateProcessor, EmailProperties emailProperties) {
        this.emailService = emailService;
        this.templateProcessor = templateProcessor;
        this.emailProperties = emailProperties;
    }

    public void sendVisitorConfirmationEmail(Visitor visitor) {
        String subject = emailProperties.getVisitorConfirmation().getSubject();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("visitorName", visitor.getName());
        placeholders.put("visitorEmail", visitor.getEmail());

        String body = templateProcessor.loadTemplate(
                emailProperties.getVisitorConfirmation().getTemplate(),
                placeholders
        );

        Email email = Email.of(
                emailProperties.getSender(),
                List.of(visitor.getEmail()),
                subject,
                body
        );

        emailService.sendEmail(email);
    }


    public void sendHostNotification(Visitor visitor, Employee host) {
        String subject = emailProperties.getHostNotification().getSubject();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("hostName", host.getName());
        placeholders.put("visitorName", visitor.getName());
        placeholders.put("visitorEmail", visitor.getEmail());

        String body = templateProcessor.loadTemplate(
                emailProperties.getHostNotification().getTemplate(),
                placeholders
        );

        Email email = Email.of(
                emailProperties.getSender(),
                List.of(host.getEmail()),
                subject,
                body
        );

        emailService.sendEmail(email);
    }
}
