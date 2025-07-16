package com.statusneo.vms.service;

import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Otp;
import com.statusneo.vms.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OtpServiceTest {

    @InjectMocks
    private OtpService otpService;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception{
        MockitoAnnotations.openMocks(this);
        Field field = OtpService.class.getDeclaredField("otpSubject");
        field.setAccessible(true);
        field.set(otpService, "Your OTP");
    }


    @Test
    void testValidateOtpSuccessAndFailure() {
        String email = "test@example.com";
        String otpValue = "654321";
        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtp(otpValue);
        otp.setExpirationTime(LocalDateTime.now().plusMinutes(10));
        when(otpRepository.findByEmailOrdered(email)).thenReturn(List.of(otp));

        boolean valid = otpService.validateOtp(email, otpValue);
        assertTrue(valid);

        valid = otpService.validateOtp(email, "wrong");
        assertFalse(valid);

        otpService.validateOtp(email, "wrong");
        otpService.validateOtp(email, "wrong");
        assertTrue(otpService.hasExceededOtpAttempts(email));
    }

    @Test
    void testMarkAndCheckEmailVerified() {
        String email = "test@example.com";
        otpService.markEmailAsVerified(email);
        assertTrue(otpService.isEmailVerified(email));
    }

    @Test
    void testEmailVerificationExpires() throws Exception {
        String email = "test@example.com";
        otpService.markEmailAsVerified(email);

        Field field = OtpService.class.getDeclaredField("verificationTimestamps");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, LocalDateTime> timestamps = (Map<String, LocalDateTime>) field.get(otpService);

        timestamps.put(email, LocalDateTime.now().minusMinutes(16));
        assertFalse(otpService.isEmailVerified(email));
    }

    @Test
    void testClearVerifiedEmailResetsAttempts() {
        String email = "test@example.com";
        otpService.validateOtp(email, "wrong");
        otpService.clearVerifiedEmail(email);
        assertFalse(otpService.hasExceededOtpAttempts(email));
    }

    @Test
    void testGetLatestOtpByEmailReturnsEmptyIfNone() {
        String email = "test@example.com";
        when(otpRepository.findByEmailOrdered(email)).thenReturn(Collections.emptyList());
        assertTrue(otpService.getLatestOtpByEmail(email).isEmpty());
    }

    @Test
    void testGenerateOtpFormat() {
        String otp = otpService.generateOtp();
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    void testSendOtpPrivateMethod() throws Exception {
        String email = "test@example.com";
        Method method = OtpService.class.getDeclaredMethod("sendOtp", String.class, String.class);
        method.setAccessible(true);
        method.invoke(otpService, email, "123456");
    }

    @Test
    void testSendOtpSavesAndSendsEmail() {
        String email = "noreply@company.com";

        otpService.sendOtp(email);

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        Otp savedOtp = otpCaptor.getValue();
        assertEquals(email, savedOtp.getEmail());
        assertNotNull(savedOtp.getOtp());
        assertTrue(savedOtp.getOtp().matches("\\d{6}"));

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of(email), sentEmail.to());
        assertEquals("Your OTP", sentEmail.subject());
        assertTrue(sentEmail.body().contains("Your OTP is: " + savedOtp.getOtp()));
    }

    @Test
    void testResendOtpWithinTwoMinutesFails() {
        String email = "visitor@gmail.com";

        Otp recentOtp = new Otp();
        recentOtp.setEmail(email);
        recentOtp.setOtp("222222");
        recentOtp.setExpirationTime(LocalDateTime.now().plusMinutes(9));

        when(otpRepository.findByEmailOrdered(email)).thenReturn(List.of(recentOtp));

        boolean result = otpService.resendOtp(email);

        assertFalse(result);
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void testResendOtpAfterTwoMinutesSucceeds() {
        String email = "noreply@company.com";

        Otp oldOtp = new Otp();
        oldOtp.setEmail(email);
        oldOtp.setOtp("123456");
        oldOtp.setExpirationTime(LocalDateTime.now().plusMinutes(10).minusMinutes(2).minusSeconds(1));

        when(otpRepository.findByEmailOrdered(email)).thenReturn(List.of(oldOtp));

        boolean result = otpService.resendOtp(email);

        assertTrue(result);

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository).save(otpCaptor.capture());
        Otp newOtp = otpCaptor.getValue();
        assertNotNull(newOtp.getOtp());
        assertEquals(email, newOtp.getEmail());
        assertTrue(newOtp.getOtp().matches("\\d{6}"));

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(emailService).sendEmail(emailCaptor.capture());
        Email sentEmail = emailCaptor.getValue();

        assertEquals("noreply@company.com", sentEmail.from());
        assertEquals(List.of(email), sentEmail.to());
        assertEquals("Your OTP", sentEmail.subject());
        assertTrue(sentEmail.body().contains("Your OTP is: " + newOtp.getOtp()));
    }

}