package com.statusneo.vms.service;

import com.statusneo.vms.dto.VerificationResult;
import com.statusneo.vms.model.Visit;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@Testcontainers
public class VisitServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private VisitService visitorService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private VisitRepository visitRepository;

    @MockitoBean
    private OtpService otpService;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

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

//        Visit registeredVisitor = visitorService.registerVisit(visit);

//        assertNotNull(registeredVisitor);
    }

    @Test
    public void testConfirmVisit_Success() {
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
        Visit updatedVisit = visitRepository.findById(visit.getId()).orElse(null);

        assertNotNull(result);
        assertTrue(result.success(), "OTP verification should be successful");
        assertFalse(result.reattempt(), "Reattempt should be false on success");
        assertEquals("OTP verified successfully", result.message());

        assertNotNull(updatedVisit, "Updated visit should not be null");
        assertTrue(updatedVisit.getIsApproved(), "Visit should be marked as approved");
    }
}

