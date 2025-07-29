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
