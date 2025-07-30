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