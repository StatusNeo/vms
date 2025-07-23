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
import com.statusneo.vms.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Value("${visitor.system.otp.subject}")
    private String otpSubject;

    private static final int OTP_EXPIRATION_MINUTES = 10;
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> verificationTimestamps = new ConcurrentHashMap<>();

    public String generatedOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
    }

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    private void sendOtp(String email, String otp) {
        emailService.sendEmail(Email.of("fromEmail", email, "Your OTP", "Your OTP is: " + otp));
    }
    public void sendOtp(String email) {
        String otp = generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpirationTime(expirationTime);

        otpRepository.save(otpEntity);
        emailService.sendEmail(Email.of(email, email, otpSubject, "Your OTP is: " + otp));
    }

    public Optional<Otp> getLatestOtpByEmail(String email) {
        List<Otp> otps = otpRepository.findByEmailOrdered(email);
        return otps.isEmpty() ? Optional.empty() : Optional.of(otps.getFirst());
    }

    public boolean validateOtp(String email, String otp) {
        Optional<Otp> latestOtp = getLatestOtpByEmail(email);
        return latestOtp.filter(value -> !value.getExpirationTime().isBefore(LocalDateTime.now()) &&
                value.getOtp().equals(otp)).isPresent();
    }

    public void markEmailAsVerified(String email) {
        verifiedEmails.put(email, true);
        verificationTimestamps.put(email, LocalDateTime.now());
    }

    public boolean isEmailVerified(String email) {
        LocalDateTime timestamp = verificationTimestamps.get(email);
        if (timestamp != null && timestamp.plusMinutes(15).isBefore(LocalDateTime.now())) {
            clearVerifiedEmail(email);
            return false;
        }
        return verifiedEmails.getOrDefault(email, false);
    }

    public void clearVerifiedEmail(String email) {
        verifiedEmails.remove(email);
        verificationTimestamps.remove(email);
    }
}