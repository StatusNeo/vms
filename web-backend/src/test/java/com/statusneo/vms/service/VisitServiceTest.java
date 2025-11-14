package com.statusneo.vms.service;

import com.statusneo.vms.config.TestConfig;
import com.statusneo.vms.dto.VerificationResult;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class VisitServiceTest {

    @Autowired
    private VisitService visitService;

    @Autowired
    private VisitRepository visitRepository;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private NotificationService notificationService;


    @Test
    public void testRegisterVisitor() {
        Visitor visitor = new Visitor();
        Visit visit = new Visit();
        visitor.setName("John Doe");
        visitor.setPhoneNumber("1234567890");
        visitor.setEmail("john.doe@example.com");
        visit.setHost("Host Name");
        visitor.setAddress("123 Street, City, Country");
        visit.setVisitDate(LocalDateTime.parse("2022-01-01T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    public void testConfirmVisit_Success() throws InterruptedException {
        Visitor visitor = new Visitor();
        visitor.setName("Anurag Sharma");
        visitor.setEmail("anurag@gmail.com");
        visitor.setPhoneNumber("9999999999");
        visitor.setAddress("123 Delhi Address");

        Visit visit = visitService.registerVisit(visitor);
        String dummyOtp = "123456";

        VerificationResult verificationResult = new VerificationResult(true, false, "OTP verified successfully");

        Mockito.when(otpService.validateOtp(any(Visit.class), eq(dummyOtp)))
                .thenReturn(verificationResult);

        VerificationResult result = visitService.confirmVisit(visit.getId(), dummyOtp);

        assertNotNull(result);
        assertTrue(result.success(), "OTP verification should be successful");
        assertFalse(result.reattempt(), "Reattempt should be false on success");
        assertEquals("OTP verified successfully", result.message());

        Visit updatedVisit = visitRepository.findById(visit.getId()).orElseThrow();
        assertTrue(updatedVisit.getIsApproved(), "Visit should be approved after successful OTP verification");
    }
}