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
package com.statusneo.vms.controller;

import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.*;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    private static final Logger logger = LoggerFactory.getLogger(VisitorController.class);

    @Autowired
    private VisitorRepository visitorRepository;
    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmployeeNameCache employeeNameCache;


    private final Map<String, Visitor> pendingVisitors = new HashMap<>();


    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam String period) {
        List<Visit> visit;
        if (period.equals("daily")) {
            visit = visitRepository.findAllByVisitDateBetween(LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now());
        } else if (period.equals("monthly")) {
            visit = visitRepository.findAllByVisitDateBetween(LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        } else {
            return ResponseEntity.badRequest().body("Invalid period");
        }
        return ResponseEntity.ok(visit);
    }

    @RequestMapping("/error")
    public String handleError() {
        return "Custom error page!";
    }

    @GetMapping("/")
    public String home() {
        return "index";  // Looks for src/main/resources/templates/index.html
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(
            @RequestParam Long visitId,
            @RequestParam String otp) {

        // Find the visit by ID
        Visit visit = visitRepository.findById(visitId)
                .orElse(null);
        if (visit == null) {
            return ResponseEntity.badRequest().body("Invalid visit ID");
        }

        boolean isValid = otpService.validateOtp(visit, otp);

        if (isValid) {
            otpService.markVisitAsVerified(visit);
            return ResponseEntity.ok("<p class=\"text-green-600 font-bold\">OTP Verified Successfully!</p>");
        } else {
            return ResponseEntity.ok("""
                <div id="otp-error-message" class="text-red-600 font-bold mb-4">
                    Invalid OTP, please try again
                </div>
                """);
        }
    }
    @GetMapping("/search")
    public String searchEmployees(@RequestParam("employee") String query, Model model) {
        logger.info("Received search request for employee: {}", query);
        List<String> employeeNames = employeeNameCache.getEmployeeNamesByPrefix(query);
        model.addAttribute("employees", employeeNames);
        return "employeeSearchResults";
    }

    @GetMapping("/refresh-employee-cache")
    public ResponseEntity<String> refreshEmployeeCache() {
        employeeNameCache.initializeCache();
        return ResponseEntity.ok("Cache refreshed");
    }


    @PostMapping("/saveVisitor")
    public ResponseEntity<String> saveVisitor(@RequestBody Visitor visitor) {
        try {
            return ResponseEntity.ok("<p class='text-green-600 font-bold'>Visitor registered successfully!</p>");
        } catch (IllegalStateException e) {
            // Custom message when visitor exists
            return ResponseEntity.ok("<p class='text-blue-600 font-bold'>Welcome back! Your visit has been recorded.</p>");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("<p class='text-red-600'>Error: " + e.getMessage() + "</p>");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVisitor(@RequestBody Visitor visitor) {
        try {
            if (visitor.getEmail() == null || visitor.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            // Register visit and send OTP
            Visit savedVisit = visitService.registerVisit(visitor);
            return ResponseEntity.ok("Visitor registered successfully. Visit ID: " + savedVisit.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registering visitor: " + e.getMessage());
        }
    }
}