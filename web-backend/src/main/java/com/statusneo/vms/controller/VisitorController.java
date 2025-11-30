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
import org.springframework.validation.FieldError;
import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.dto.VerificationResult;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.service.GraphDirectoryService;
import com.statusneo.vms.service.OtpService;
import com.statusneo.vms.service.VisitService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class VisitorController {

    private static final Logger logger = LoggerFactory.getLogger(VisitorController.class);

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private EmployeeNameCache employeeNameCache;

    @Autowired
    private GraphDirectoryService graphDirectoryService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("visitor", new Visitor());
        return "index";
    }

    // Add this method to serve the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("visitor", new Visitor());
        model.addAttribute("employees", employeeRepository.findAll()); // or employeeService.getAllEmployees()
        return "visitorRegistration";
    }

    @PostMapping("/register")
    public String registerVisitor(
            @Valid @ModelAttribute("visitor") Visitor visitor,
            BindingResult bindingResult,
            @RequestParam(value = "host", required = false) String host,
            @RequestParam(value = "employee", required = false) String employee,
            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
            Model model) {

        logger.info("Processing visitor registration for: {}", visitor.getEmail());

        // Check for validation errors - Spring validation pattern
        if (bindingResult.hasErrors()) {
            logger.warn("Form validation failed with {} errors", bindingResult.getErrorCount());

            // Add field errors and visitor to model for display in template
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            model.addAttribute("fieldErrors", fieldErrors);
            model.addAttribute("visitor", visitor);
            model.addAttribute("employees", employeeRepository.findAll()); // Add employees for dropdown

            if (hxRequest != null && hxRequest.equals("true")) {
                return "fragments/validation-errors"; // HTMX error fragment
            }
            // Return the registration form template to show validation errors
            return "visitorRegistration";
        }

        resolveAndSetHost(visitor, host, employee);

        Visit savedVisit = visitService.registerVisit(visitor);
        model.addAttribute("visitId", savedVisit.getId());
        model.addAttribute("visit", savedVisit);

        if (hxRequest != null && hxRequest.equals("true")) {
            return "fragments/otp-modal";
        }

        // Regular form submission - return result view
        return "visitorRegistrationResult";
    }

    @GetMapping("/report")
    public String getReport(@RequestParam String period, Model model) {
        List<Visit> visits;
        LocalDateTime now = LocalDateTime.now();

        if (period.equals("daily")) {
            visits = visitRepository.findAllByVisitDateBetween(now.toLocalDate().atStartOfDay(), now);
        } else if (period.equals("monthly")) {
            visits = visitRepository.findAllByVisitDateBetween(now.minusMonths(1), now);
        } else {
            return "error/400";
        }
        model.addAttribute("visits", visits);
        return "report";
    }

    @PostMapping("/confirm-visit")
    public Object confirmVisit(@RequestParam("visitId") Long visitId,
                               @RequestParam("otpCode") String otpCode,
                               @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                               Model model) {
        VerificationResult result = visitService.confirmVisit(visitId, otpCode);
        model.addAttribute("result", result);
        model.addAttribute("visitId", visitId);

        if (hxRequest != null && hxRequest.equals("true")) {
            if (result.success()) {
                Visit visit = visitRepository.findById(visitId)
                        .orElseThrow(() -> new IllegalArgumentException("Visit not found"));
                model.addAttribute("visit", visit);
                return "fragments/success-message";
            } else {
                if (!result.reattempt()) {
                    return ResponseEntity.ok().header("HX-Redirect", "/").build();
                }

                Visit visit = visitRepository.findById(visitId)
                        .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

                VerificationResult resendResult = otpService.generateOtp(visit, false);

                if (resendResult.success()) {
                    model.addAttribute("serverMessage", "Invalid OTP. A new OTP has been sent to your email.");
                } else {
                    model.addAttribute("serverMessage", resendResult.message());
                }

                return "fragments/otp-modal";
            }
        }

        if (result.success()) {
            return "confirmation-modal";
        } else if (!result.reattempt()) {
            return "redirect:/";
        } else {
            Visit visit = visitRepository.findById(visitId)
                    .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

            VerificationResult resendResult = otpService.generateOtp(visit, false);
            if (resendResult.success()) {
                model.addAttribute("serverMessage", "Invalid OTP. A new OTP has been sent to your email.");
            } else {
                model.addAttribute("serverMessage", resendResult.message());
            }

            model.addAttribute("visitId", visitId);
            return "otp-modal";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam("visitId") Long visitId,
                            @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                            Model model) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

        VerificationResult result = otpService.generateOtp(visit);

        model.addAttribute("result", result);
        model.addAttribute("visitId", visitId);
        model.addAttribute("serverMessage", result.message());

        // Handle both HTMX and regular requests
        if (hxRequest != null && hxRequest.equals("true")) {
            return "fragments/otp-modal";
        }
        return "otp-modal";
    }

    /**
     * Resolve the submitted employee string (which may be an id or a name) and set the Visitor.host
     */
    private void resolveAndSetHost(Visitor visitor, String hostIdStr, String employeeStr) {
        if (visitor == null) return;

        // If explicit host id provided, prefer it
        if (hostIdStr != null && !hostIdStr.isBlank()) {
            try {
                Long id = Long.valueOf(hostIdStr.trim());
                employeeRepository.findById(id).ifPresent(visitor::setHost);
                return;
            } catch (NumberFormatException ignored) {
                // fall through to name resolution
            }
        }

        // Fall back to name-based resolution if provided
        if (employeeStr == null || employeeStr.isBlank()) return;
        String trimmed = employeeStr.trim();
        employeeRepository.findByNameIgnoreCase(trimmed).ifPresent(visitor::setHost);
    }
}