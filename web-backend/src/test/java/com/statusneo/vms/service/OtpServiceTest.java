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

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

        // Setup test data
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
    void testValidateOtpSuccessAndFailure() {
        String otpValue = "654321";

        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq(otpValue), any(LocalDateTime.class)))
                .thenReturn(true);
        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq("wrong"), any(LocalDateTime.class)))
                .thenReturn(false);

        VerificationResult result = otpService.validateOtp(testVisit, otpValue);
        assertTrue(result.success());
        assertFalse(result.reattempt());
        assertEquals("OTP verified successfully", result.message());

        result = otpService.validateOtp(testVisit, "wrong");
        assertFalse(result.success());
        assertTrue(result.reattempt());
        assertEquals("Invalid OTP. Please try again.", result.message());


        result = otpService.validateOtp(testVisit, "wrong");
        assertFalse(result.success());
        assertFalse(result.reattempt());
        assertEquals("Maximum attempts exceeded.", result.message());

        assertTrue(otpService.hasExceededOtpAttempts(testVisit));
    }


    @Test
    void testGetLatestOtpByVisitReturnsEmptyIfNone() {
        // Updated to use the optimized repository method
        when(otpRepository.findFirstByVisitOrderByCreatedAtDesc(testVisit)).thenReturn(Optional.empty());
        assertTrue(otpService.getLatestOtpByVisit(testVisit).isEmpty());
    }

    @Test
    void testResendOtpWithinTwoMinutesFails() {
        Otp recentOtp = new Otp();
        recentOtp.setEmail(testVisitor.getEmail());
        recentOtp.setOtp("222222");
        recentOtp.setExpirationTime(LocalDateTime.now().plusMinutes(9));
        recentOtp.setVisit(testVisit);
        recentOtp.setCreatedAt(LocalDateTime.now().minusMinutes(1)); // Recent OTP
        recentOtp.setResendCount(0);

        // Updated to use the optimized repository method
        when(otpRepository.findFirstByVisitOrderByCreatedAtDesc(testVisit)).thenReturn(Optional.of(recentOtp));

        boolean result = otpService.canResendOtp(testVisit);

        assertFalse(result);
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void testMarkAndCheckVisitVerified() {
        otpService.markVisitAsVerified(testVisit);
        assertTrue(otpService.isVisitVerified(testVisit));
    }

    @Test
    void testVisitVerificationExpires() throws Exception {
        otpService.markVisitAsVerified(testVisit);

        Field field = OtpService.class.getDeclaredField("verificationTimestamps");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Long, LocalDateTime> timestamps = (Map<Long, LocalDateTime>) field.get(otpService);

        timestamps.put(testVisit.getId(), LocalDateTime.now().minusMinutes(16));
        assertFalse(otpService.isVisitVerified(testVisit));
    }

    @Test
    void testClearVerifiedVisitResetsAttempts() {
        otpService.validateOtp(testVisit, "wrong");
        otpService.clearVerifiedVisit(testVisit);
        assertFalse(otpService.hasExceededOtpAttempts(testVisit));
    }

    @Test
    void testGetLatestOtpByVisitIdReturnsEmptyIfNone() {
        // Updated to use the optimized repository method
        when(otpRepository.findFirstByVisitIdOrderByCreatedAtDesc(testVisit.getId())).thenReturn(Optional.empty());
        assertTrue(otpService.getLatestOtpByVisitId(testVisit.getId()).isEmpty());
    }

    @Test
    void testGenerateOtpFormat() {
        String otp = otpService.generateOtp();
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testSendOtpSavesAndSendsEmail() {
        otpService.sendOtp(testVisit);

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        Otp savedOtp = otpCaptor.getValue();

        assertEquals(testVisitor.getEmail(), savedOtp.getEmail());
        assertEquals(testVisit, savedOtp.getVisit());
        assertNotNull(savedOtp.getOtp());
        assertTrue(savedOtp.getOtp().matches("\\d{6}"));
        assertNotNull(savedOtp.getExpirationTime());

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals(testVisitor.getEmail(), sentEmail.from());
        assertEquals(List.of(testVisitor.getEmail()), sentEmail.to());
        assertEquals("Your OTP", sentEmail.subject());
        assertTrue(sentEmail.body().contains("Your OTP for visit to " + testVisit.getHost() + " is: " + savedOtp.getOtp()));
    }

    @Test
    void testResendOtpAfterTwoMinutesSucceeds() {
        Otp oldOtp = new Otp();
        oldOtp.setEmail(testVisitor.getEmail());
        oldOtp.setOtp("123456");
        oldOtp.setExpirationTime(LocalDateTime.now().plusMinutes(7));
        oldOtp.setVisit(testVisit);
        oldOtp.setCreatedAt(LocalDateTime.now().minusMinutes(3));
        oldOtp.setResendCount(0);

        // Updated to use the optimized repository method
        when(otpRepository.findFirstByVisitOrderByCreatedAtDesc(testVisit)).thenReturn(Optional.of(oldOtp));

        boolean result = otpService.canResendOtp(testVisit);

        assertTrue(result);

        // Capture all save calls (expecting 2: one for new OTP, one for updating resend count)
        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository, times(2)).save(otpCaptor.capture());

        List<Otp> savedOtps = otpCaptor.getAllValues();
        assertEquals(2, savedOtps.size());

        // First save should be the new OTP
        Otp newOtp = savedOtps.get(0);
        assertNotNull(newOtp.getOtp());
        assertEquals(testVisitor.getEmail(), newOtp.getEmail());
        assertEquals(testVisit, newOtp.getVisit());
        assertTrue(newOtp.getOtp().matches("\\d{6}"));
        assertNotEquals("123456", newOtp.getOtp()); // Should be different from old OTP

        // Second save should be the old OTP with updated resend count
        Otp updatedOldOtp = savedOtps.get(1);
        assertEquals("123456", updatedOldOtp.getOtp()); // Same OTP value as original
        assertEquals(1, updatedOldOtp.getResendCount()); // Resend count should be incremented

        // Verify email was sent
        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals(testVisitor.getEmail(), sentEmail.from());
        assertEquals(List.of(testVisitor.getEmail()), sentEmail.to());
        assertEquals("Your OTP", sentEmail.subject());
        assertTrue(sentEmail.body().contains("Your OTP for visit to " + testVisit.getHost() + " is: " + newOtp.getOtp()));
    }

    @Test
    void testResendOtpFailsWhenMaxResendCountReached() {
        Otp maxResendOtp = new Otp();
        maxResendOtp.setEmail(testVisitor.getEmail());
        maxResendOtp.setOtp("123456");
        maxResendOtp.setExpirationTime(LocalDateTime.now().plusMinutes(7));
        maxResendOtp.setVisit(testVisit);
        maxResendOtp.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        maxResendOtp.setResendCount(2); // Max resend count reached

        // Updated to use the optimized repository method
        when(otpRepository.findFirstByVisitOrderByCreatedAtDesc(testVisit)).thenReturn(Optional.of(maxResendOtp));

        boolean result = otpService.canResendOtp(testVisit);

        assertFalse(result);
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void testValidateOtpWithExpiredOtp() {
        String otpValue = "654321";

        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq(otpValue), any(LocalDateTime.class)))
                .thenReturn(false);

        VerificationResult result1 = otpService.validateOtp(testVisit, otpValue);
        assertFalse(result1.success());
        assertTrue(result1.reattempt());
        assertEquals("Invalid OTP. Please try again.", result1.message());

        VerificationResult result2 = otpService.validateOtp(testVisit, otpValue);
        assertFalse(result2.success());
        assertFalse(result2.reattempt());
        assertEquals("Maximum attempts exceeded.", result2.message());

        VerificationResult result3 = otpService.validateOtp(testVisit, otpValue);
        assertFalse(result3.success());
        assertFalse(result3.reattempt());
        assertEquals("Maximum attempts exceeded.", result3.message());

        assertTrue(otpService.hasExceededOtpAttempts(testVisit));
    }

    @Test
    void testMultipleVisitsHaveIndependentOtpTracking() {
        // Create second visit
        Visit secondVisit = new Visit();
        secondVisit.setId(2L);
        secondVisit.setVisitor(testVisitor);
        secondVisit.setHost("Bob Wilson");
        secondVisit.setVisitDate(LocalDateTime.now());

        // Mock existsByVisitAndOtpAndExpirationTimeAfter to return false for wrong attempts
        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq("wrong1"), any(LocalDateTime.class))).thenReturn(false);
        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq("wrong2"), any(LocalDateTime.class))).thenReturn(false);
        when(otpRepository.existsByVisitAndOtpAndExpirationTimeAfter(eq(testVisit), eq("wrong3"), any(LocalDateTime.class))).thenReturn(false);

        // Exceed attempts for first visit
        otpService.validateOtp(testVisit, "wrong1");
        otpService.validateOtp(testVisit, "wrong2");
        otpService.validateOtp(testVisit, "wrong3");

        // Second visit should not be affected
        assertFalse(otpService.hasExceededOtpAttempts(secondVisit));
        assertTrue(otpService.hasExceededOtpAttempts(testVisit));
    }
}
