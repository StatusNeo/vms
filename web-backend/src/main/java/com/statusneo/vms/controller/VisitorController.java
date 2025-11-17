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
    public String home() {
        return "index";
    }

    @GetMapping("/search")
    public String searchEmployees(@RequestParam("employee") String query, Model model) {
        logger.info("Received search request for employee: {}", query);
        List<String> names = employeeNameCache.getEmployeeNamesByPrefix(query == null ? "" : query);
        model.addAttribute("employees", names);
        return "employeeSearchResults";
    }

    // Optional: Add MVC version of report page if you want a web UI for reports
    @GetMapping("/report-page")
    public String showReportPage(@RequestParam(defaultValue = "daily") String period, Model model) {
        logger.info("Displaying report page for period: {}", period);
        model.addAttribute("period", period);
        return "report"; // You'll need to create report.jte template
    }

    // Optional: Add MVC version of cache refresh status
    @GetMapping("/cache-status")
    public String showCacheStatus(Model model) {
        model.addAttribute("cacheStatus", "Employee cache is active");
        return "cache-status"; // You'll need to create cache-status.jte template
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
            return "fragments/otp-modal";
        }

        // For regular form submission (fallback)
        return "otp-modal";
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
                // If no more reattempts allowed, tell HTMX to redirect to the entry page
                if (!result.reattempt()) {
                    return ResponseEntity.ok().header("HX-Redirect", "/").build();
                }

                // Auto-resend OTP when a failed attempt occurred and reattempts remain
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

        // For regular form submission (fallback):
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
    public String resendOtp(@RequestParam("visitId") Long visitId, Model model) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found"));

        VerificationResult result = otpService.generateOtp(visit);

        model.addAttribute("result", result);
        model.addAttribute("visitId", visitId);
        model.addAttribute("serverMessage", result.message());
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