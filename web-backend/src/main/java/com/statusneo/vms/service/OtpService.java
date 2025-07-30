
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
    private final Map<Long, Boolean> verifiedVisits = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> verificationTimestamps = new ConcurrentHashMap<>();
    private final Map<Long, Integer> otpAttempts = new ConcurrentHashMap<>();
    private static final int MAX_OTP_ATTEMPTS = 2;

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    /**
     * Sends OTP for a specific visit
     * @param visit The visit entity for which OTP is being sent
     */
    public void sendOtp(Visit visit) {
        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        Otp otpEntity = new Otp();
        otpEntity.setEmail(visit.getVisitor().getEmail());
        otpEntity.setOtp(otp);
        otpEntity.setExpirationTime(expirationTime);
        otpEntity.setVisit(visit);

        otpRepository.save(otpEntity);

        // Send email to visitor
        String visitorEmail = visit.getVisitor().getEmail();
        emailService.sendEmail(Email.of(visitorEmail, visitorEmail, otpSubject,
                "Your OTP for visit to " + visit.getHost() + " is: " + otp));
    }

    /**
     * Get latest OTP by visit
     * @param visit The visit entity
     * @return Optional of the latest OTP
     */
    public Optional<Otp> getLatestOtpByVisit(Visit visit) {
        return otpRepository.findFirstByVisitOrderByCreatedAtDesc(visit);
    }


    /**
     * Get latest OTP by visit ID
     * @param visitId The visit ID
     * @return Optional of the latest OTP
     */
    public Optional<Otp> getLatestOtpByVisitId(Long visitId) {
        return otpRepository.findFirstByVisitIdOrderByCreatedAtDesc(visitId);
    }
    /**
     * Check if OTP can be resent for a visit
     * @param visit The visit entity
     * @return true if OTP can be resent, false otherwise
     */
    public boolean canResendOtp(Visit visit) {
        Optional<Otp> latestOtp = getLatestOtpByVisit(visit);
        if (latestOtp.isPresent()) {
            Otp otp = latestOtp.get();
            // Check if 2 minutes have passed since last OTP
            LocalDateTime lastSent = otp.getCreatedAt();
            if (lastSent.plusMinutes(2).isAfter(LocalDateTime.now())) {
                return false;
            }

            // Check resend count limit
            if (otp.getResendCount() >= 2) {
                return false;
            }
        }

        sendOtp(visit);

        // Update resend count for the latest OTP
        latestOtp = getLatestOtpByVisit(visit);
        if (latestOtp.isPresent()) {
            Otp otp = latestOtp.get();
            otp.incrementResendCount();
            otpRepository.save(otp);
        }

        return true;
    }

    /**
     * Validate OTP for a specific visit
     * @param visit The visit entity
     * @param otpCode The OTP code to validate
     * @return true if valid, false otherwise
     */
    public boolean validateOtp(Visit visit, String otpCode) {
        Long visitId = visit.getId();
        int attempts = otpAttempts.getOrDefault(visitId, 0);

        if (attempts >= MAX_OTP_ATTEMPTS) {
            return false;
        }
        boolean valid = otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(visit, otpCode, LocalDateTime.now());

        if (!valid) {
            otpAttempts.put(visitId, attempts + 1);
        } else {
            otpAttempts.remove(visitId);
        }

        return valid;
    }

    /**
     * Mark visit as verified
     * @param visit The visit entity to mark as verified
     */
    public void markVisitAsVerified(Visit visit) {
        Long visitId = visit.getId();
        otpAttempts.remove(visitId);
        verifiedVisits.put(visitId, true);
        verificationTimestamps.put(visitId, LocalDateTime.now());
    }

    /**
     * Check if visit is verified
     * @param visit The visit entity to check
     * @return true if verified and not expired, false otherwise
     */
    public boolean isVisitVerified(Visit visit) {
        Long visitId = visit.getId();
        LocalDateTime timestamp = verificationTimestamps.get(visitId);

        if (timestamp != null && timestamp.plusMinutes(15).isBefore(LocalDateTime.now())) {
            clearVerifiedVisit(visit);
            return false;
        }

        return verifiedVisits.getOrDefault(visitId, false);
    }

    /**
     * Clear verified status for a visit
     * @param visit The visit entity to clear
     */
    public void clearVerifiedVisit(Visit visit) {
        Long visitId = visit.getId();
        verifiedVisits.remove(visitId);
        verificationTimestamps.remove(visitId);
        otpAttempts.remove(visitId);
    }

    /**
     * Check if visit has exceeded OTP attempts
     * @param visit The visit entity to check
     * @return true if exceeded, false otherwise
     */
    public boolean hasExceededOtpAttempts(Visit visit) {
        return otpAttempts.getOrDefault(visit.getId(), 0) >= MAX_OTP_ATTEMPTS;
    }

}
