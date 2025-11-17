/*
 * Copyright [2025] StatusNeo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: StatusNeo

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
package com.statusneo.vms.service;

import com.statusneo.vms.dto.VerificationResult;
import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Otp;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 @Service
 public class OtpService {

     private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

     private final OtpRepository otpRepository;
     private final EmailService emailService;
     private final String otpSubject;
     private final String systemFrom;

     public OtpService(OtpRepository otpRepository, EmailService emailService,
                      @Value("${VMS_SYSTEM_EMAIL:${vms.system-email:noreply@company.com}}") String systemFrom,
                      @Value("${app.otp.subject:Your OTP}") String otpSubject) {
         this.otpRepository = otpRepository;
         this.emailService = emailService;
         this.otpSubject = otpSubject;
         this.systemFrom = systemFrom;
     }

     private static final int OTP_EXPIRATION_MINUTES = 3;
     private static final int MAX_OTP_ATTEMPTS = 2;
     private static final int MAX_RESEND_COUNT = 2;
     private static final int RESEND_COOLDOWN_MINUTES = 2;

     private final Map<Long, Integer> otpAttempts = new ConcurrentHashMap<>();

     /**
      * Public Method 1: Generate or resend OTP for a visit
      */
     public VerificationResult generateOtp(Visit visit) {
         return generateOtp(visit, true, false);
     }

     /**
      * Generate OTP with control over whether to reset in-memory attempt counters.
      * If resetAttempts is true the per-visit attempt counter is removed when a new OTP is generated.
      */
     public VerificationResult generateOtp(Visit visit, boolean resetAttempts) {
         return generateOtp(visit, resetAttempts, false);
     }

     /**
      * Generate OTP with control over whether to reset in-memory attempt counters and whether to bypass cooldown.
      * If resetAttempts is true the per-visit attempt counter is removed when a new OTP is generated.
      * If bypassCooldown is true the cooldown check is skipped (useful for automatic resends after failed validation).
      */
     public VerificationResult generateOtp(Visit visit, boolean resetAttempts, boolean bypassCooldown) {
         Optional<Otp> latestOtpOpt = getLatestOtpByVisit(visit);

         if (latestOtpOpt.isPresent()) {
             Otp latestOtp = latestOtpOpt.get();

             // Check cooldown (skip if bypassCooldown requested)
             if (!bypassCooldown && latestOtp.getCreatedAt().plusMinutes(RESEND_COOLDOWN_MINUTES).isAfter(LocalDateTime.now())) {
                 return new VerificationResult(false, true,
                         "Incorrect OTP Check and try again");
             }

             // Check resend limit
             if (latestOtp.getResendCount() >= MAX_RESEND_COUNT) {
                 return new VerificationResult(false, false,
                         "OTP resend limit reached.");
             }
         }

         // Generate new OTP
         String otp = generateOtpCode();
         LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

         Otp otpEntity = new Otp();
         otpEntity.setEmail(visit.getVisitor().getEmail());
         otpEntity.setOtp(otp);
         otpEntity.setExpirationTime(expirationTime);
         otpEntity.setVisit(visit);

         latestOtpOpt.ifPresent(value -> otpEntity.setResendCount(value.getResendCount() + 1));

         otpRepository.save(otpEntity);

         // Reset attempt counter for this visit when a new OTP is generated only if requested
         if (resetAttempts && visit.getId() != null) {
             otpAttempts.remove(visit.getId());
         }

         // Send email
         String visitorEmail = visit.getVisitor().getEmail();
         // Resolve host name safely: prefer visit.host (string), fall back to visitor.host.name if present
         String hostName = visit.getHost();
         if ((hostName == null || hostName.isBlank()) && visit.getVisitor() != null && visit.getVisitor().getHost() != null) {
             hostName = visit.getVisitor().getHost().getName();
         }
         if (hostName == null) {
             hostName = "your host"; // sensible default to avoid 'null' in emails
         }

         String otpBody = "Your OTP for visit to " + hostName + " is: " + otp;
         boolean sent = emailService.sendEmail(Email.of(systemFrom, List.of(visitorEmail), otpSubject, otpBody));
         if (!sent) {
             logger.warn("Failed to send OTP email to {} from {}", visitorEmail, systemFrom);
         }

         return new VerificationResult(true, true, "OTP sent successfully.");
     }

     /**
      * Public Method 2: Validate OTP
      */
     public VerificationResult validateOtp(Visit visit, String otpCode) {
         Long visitId = visit.getId();
         int attempts = otpAttempts.getOrDefault(visitId, 0);

         if (attempts >= MAX_OTP_ATTEMPTS) {
             return new VerificationResult(false, false, "Maximum attempts exceeded.");
         }

         boolean valid = otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(
                 visit, otpCode, LocalDateTime.now());

         if (valid) {
             otpAttempts.remove(visitId);
             return new VerificationResult(true, false, "OTP verified successfully.");
         } else {
             otpAttempts.put(visitId, attempts + 1);
             boolean canRetry = (attempts + 1) < MAX_OTP_ATTEMPTS;
             return new VerificationResult(false, canRetry,
                     canRetry ? "Invalid OTP. Please try again." : "Maximum attempts exceeded.");
         }
     }

     // ======== Private Helpers ========

     private String generateOtpCode() {
         Random random = new Random();
         return String.valueOf(100000 + random.nextInt(900000)); // 6-digit OTP
     }

     private Optional<Otp> getLatestOtpByVisit(Visit visit) {
         return otpRepository.findFirstByVisitOrderByCreatedAtDesc(visit);
     }
 }
