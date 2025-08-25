
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
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

 @Service
 public class OtpService {

     private final OtpRepository otpRepository;
     private final EmailService emailService;
     private final String otpSubject;

     public OtpService(OtpRepository otpRepository, EmailService emailService,
                       @Value("${app.otp.subject:Your OTP}") String otpSubject) {
         this.otpRepository = otpRepository;
         this.emailService = emailService;
         this.otpSubject = otpSubject;
     }

     private static final int OTP_EXPIRATION_MINUTES = 10;
     private static final int MAX_OTP_ATTEMPTS = 2;
     private static final int MAX_RESEND_COUNT = 2;
     private static final int RESEND_COOLDOWN_MINUTES = 2;

     private final Map<Long, Integer> otpAttempts = new ConcurrentHashMap<>();

     /**
      * Public Method 1: Generate or resend OTP for a visit
      */
     public VerificationResult generateOtp(Visit visit) {
         Optional<Otp> latestOtpOpt = getLatestOtpByVisit(visit);

         if (latestOtpOpt.isPresent()) {
             Otp latestOtp = latestOtpOpt.get();

             // Check cooldown
             if (latestOtp.getCreatedAt().plusMinutes(RESEND_COOLDOWN_MINUTES).isAfter(LocalDateTime.now())) {
                 return new VerificationResult(false, true,
                         "Please wait before requesting a new OTP.");
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

         if (latestOtpOpt.isPresent()) {
             otpEntity.setResendCount(latestOtpOpt.get().getResendCount() + 1);
         }

         otpRepository.save(otpEntity);

         // Send email
         String visitorEmail = visit.getVisitor().getEmail();
         emailService.sendEmail(Email.of(visitorEmail, visitorEmail, otpSubject,
                 "Your OTP for visit to " + visit.getHost() + " is: " + otp));

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
