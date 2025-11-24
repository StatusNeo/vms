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
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.service.GraphDirectoryService;
import com.statusneo.vms.service.OtpService;
import com.statusneo.vms.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    //    @RequestMapping("/error")
    public String handleError() {
        return "Custom error page!";
    }

    @GetMapping("/")
    public String home() {
        return "index";  // Looks for src/main/resources/templates/simple.html
    }


    @GetMapping("/refresh-employee-cache")
    public ResponseEntity<String> refreshEmployeeCache() {
        employeeNameCache.initializeCache();
        return ResponseEntity.ok("Cache refreshed");
    }

    @PostMapping("/register")
    public String registerVisitor(@ModelAttribute Visitor visitor,
                                  @RequestParam(value = "host", required = false) String host,
                                  @RequestParam(value = "employee", required = false) String employee,
                                  @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                                  Model model) {
        // prefer explicit host id, fall back to name
        resolveAndSetHost(visitor, host, employee);
        Visit savedVisit = visitService.registerVisit(visitor);
        model.addAttribute("visitId", savedVisit.getId());

        // If it's an HTMX request, just return the modal fragment
        if (hxRequest != null && hxRequest.equals("true")) {
            // JTE doesn't use Thymeleaf fragment syntax ("::"). Return the template name
            // that corresponds to src/main/jte/fragments/otp-modal.jte
            return "fragments/otp-modal";
        }

        // For regular form submission (fallback)
        return "otp-modal";
    }

    // Updated to return Object so we can return ResponseEntity for HTMX redirects
    @PostMapping("/confirm-visit")
    public Object confirmVisit(@RequestParam("visitId") Long visitId,
                               @RequestParam("otpCode") String otpCode,
                               @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                               Model model) {
        VerificationResult result = visitService.confirmVisit(visitId, otpCode);
        model.addAttribute("result", result);
        model.addAttribute("visitId", visitId);

        // If it's an HTMX request, return a fragment or an HX-Redirect when attempts exhausted
        if (hxRequest != null && hxRequest.equals("true")) {
            if (result.success()) {
                // Pass the visit to get visitor details for success message
                Visit visit = visitRepository.findById(visitId)
                        .orElseThrow(() -> new IllegalArgumentException("Visit not found"));
                model.addAttribute("visit", visit);
                // Return the JTE template for success message
                return "fragments/success-message";
            } else {
                // If no more reattempts allowed, tell HTMX to redirect to the entry page
                if (!result.reattempt()) {
                    return ResponseEntity.ok().header("HX-Redirect", "/").build();
                }

                // Auto-resend OTP when a failed attempt occurred and reattempts remain
                Visit visit = visitRepository.findById(visitId)
                        .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

                VerificationResult resendResult = otpService.generateOtp(visit, false); // don't reset attempt counter

                // Decide the message to show in the modal: prefer an explicit resend message when OTP re-sent successfully
                if (resendResult.success()) {
                    model.addAttribute("serverMessage", "Invalid OTP. A new OTP has been sent to your email.");
                } else {
                    // If resend failed (cooldown or limit), show that message instead
                    model.addAttribute("serverMessage", resendResult.message());
                }

                // Re-show the otp modal with an error message so HTMX swaps it in place
                return "fragments/otp-modal";
            }
        }

        // For regular form submission (fallback):
        if (result.success()) {
            return "confirmation-modal";
        } else if (!result.reattempt()) {
            // Attempts exhausted: redirect to blank visitor entry form
            return "redirect:/";
        } else {
            // Auto-resend for non-HTMX fallback as well
            Visit visit = visitRepository.findById(visitId)
                    .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

            VerificationResult resendResult = otpService.generateOtp(visit, false);
            if (resendResult.success()) {
                model.addAttribute("serverMessage", "Invalid OTP. A new OTP has been sent to your email.");
            } else {
                model.addAttribute("serverMessage", resendResult.message());
            }

            // Re-show otp page with message for non-HTMX fallback
            model.addAttribute("visitId", visitId);
            return "otp-modal";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam("visitId") Long visitId, Model model) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

        VerificationResult result = otpService.generateOtp(visit);

        model.addAttribute("result", result);
        model.addAttribute("visitId", visitId);
        model.addAttribute("serverMessage", result.message());
        // For HTMX flows this should probably return the otp modal again so the UI is updated.
        return "fragments/otp-modal";
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
