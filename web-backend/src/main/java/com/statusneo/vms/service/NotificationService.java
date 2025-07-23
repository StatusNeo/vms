/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
