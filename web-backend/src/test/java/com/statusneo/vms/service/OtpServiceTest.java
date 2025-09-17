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
 */

package com.statusneo.vms.service;

import com.statusneo.vms.dto.VerificationResult;
import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Otp;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtpServiceTest {

    private OtpService otpService;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    private Visit testVisit;
    private Visitor testVisitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        otpService = new OtpService(otpRepository, emailService, "Your OTP");

        testVisitor = new Visitor();
        testVisitor.setId(1L);
        testVisitor.setName("John Doe");
        testVisitor.setEmail("test@example.com");
        testVisitor.setPhoneNumber("1234567890");

        testVisit = new Visit();
        testVisit.setId(1L);
        testVisit.setVisitor(testVisitor);
        testVisit.setHost("Jane Smith");
        testVisit.setVisitDate(LocalDateTime.now());
    }

    @Test
    void testGenerateOtpFirstTimeSuccess() {
        when(otpRepository.findFirstByVisitOrderByCreatedAtDesc(testVisit)).thenReturn(Optional.empty());

        VerificationResult result = otpService.generateOtp(testVisit);

        assertTrue(result.success());
        assertEquals("OTP sent successfully.", result.message());

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        Otp savedOtp = otpCaptor.getValue();

        assertEquals(testVisitor.getEmail(), savedOtp.getEmail());
        assertEquals(testVisit, savedOtp.getVisit());
        assertNotNull(savedOtp.getOtp());
        assertTrue(savedOtp.getOtp().matches("\\d{6}"));

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).sendEmail(emailCaptor.capture());
        assertTrue(emailCaptor.getValue().body().contains("Your OTP for visit to " + testVisit.getHost()));
    }

    @Test
    void testGenerateOtpFailsDueToCooldown() {
        Otp recentOtp = new Otp();
        recentOtp.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        recentOtp.setResendCount(0);

        when(otpRepository.findFirstByVisitOrderByCreatedAtDesc(testVisit)).thenReturn(Optional.of(recentOtp));

        VerificationResult result = otpService.generateOtp(testVisit);

        assertFalse(result.success());
        assertEquals("Please wait before requesting a new OTP.", result.message());
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void testGenerateOtpFailsDueToMaxResendCount() {
        Otp otp = new Otp();
        otp.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        otp.setResendCount(2); // Max reached

        when(otpRepository.findFirstByVisitOrderByCreatedAtDesc(testVisit)).thenReturn(Optional.of(otp));

        VerificationResult result = otpService.generateOtp(testVisit);

        assertFalse(result.success());
        assertEquals("OTP resend limit reached.", result.message());
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void testValidateOtpSuccessAndFailure() {
        String correctOtp = "654321";

        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq(correctOtp), any(LocalDateTime.class)))
                .thenReturn(true);
        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq("wrong"), any(LocalDateTime.class)))
                .thenReturn(false);

        VerificationResult result = otpService.validateOtp(testVisit, correctOtp);
        assertTrue(result.success());
        assertEquals("OTP verified successfully.", result.message());

        result = otpService.validateOtp(testVisit, "wrong");
        assertFalse(result.success());
        assertTrue(result.reattempt());
        assertEquals("Invalid OTP. Please try again.", result.message());

        result = otpService.validateOtp(testVisit, "wrong");
        assertFalse(result.success());
        assertFalse(result.reattempt());
        assertEquals("Maximum attempts exceeded.", result.message());
    }

    @Test
    void testValidateOtpBlocksAfterTwoAttempts() {
        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq("wrong"), any(LocalDateTime.class)))
                .thenReturn(false);

        otpService.validateOtp(testVisit, "wrong");
        otpService.validateOtp(testVisit, "wrong");
        VerificationResult result = otpService.validateOtp(testVisit, "wrong");

        assertFalse(result.success());
        assertFalse(result.reattempt());
        assertEquals("Maximum attempts exceeded.", result.message());
    }
}
