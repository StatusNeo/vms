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
import com.statusneo.vms.dto.VerificationResult;
import com.statusneo.vms.model.Employee;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import com.statusneo.vms.service.*;
import gg.jte.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
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


    @GetMapping("/search")
    public String searchEmployees(@RequestParam("employee") String query, Model model) {
        logger.info("Received search request for employee: {}", query);
        List<Employee> employees = employeeService.searchEmployeesByName(query);
        model.addAttribute("employees", employees);
        return "employeeSearchResults";
    }

    @GetMapping("/refresh-employee-cache")
    public ResponseEntity<String> refreshEmployeeCache() {
        employeeNameCache.initializeCache();
        return ResponseEntity.ok("Cache refreshed");
    }


    @PostMapping("/saveVisitor")
    public String saveVisitor(@ModelAttribute Visitor visitor, Model model) {
        try {
            visitService.registerVisit(visitor);
            model.addAttribute("success", true);
            model.addAttribute("message", "Visitor registered successfully!");
        } catch (IllegalStateException e) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Welcome back! Your visit has been recorded.");
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "visitorRegistrationResult";
    }

    @PostMapping("/register")
    public String registerVisitor(@ModelAttribute Visitor visitor, Model model) {
        Visit savedVisit = visitService.registerVisit(visitor);
        model.addAttribute("visitId", savedVisit.getId());
        return "visitorOtpForm";
    }

    @PostMapping("/confirm-visit")
    public String confirmVisit(@RequestParam("visitId") Long visitId,
                               @RequestParam("otpCode") String otpCode,
                               Model model) {
        VerificationResult result = visitService.confirmVisit(visitId, otpCode);
        model.addAttribute("result", result);
        model.addAttribute("visitId", visitId);
        return "visitConfirmationResult";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam("visitId") Long visitId, Model model) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

        VerificationResult result = otpService.generateOtp(visit);

        model.addAttribute("result", result);
        model.addAttribute("visitId", visitId);
        return "visitConfirmationResult";
    }
}