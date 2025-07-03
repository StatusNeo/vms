package com.statusneo.vms.service;

import com.statusneo.vms.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
//@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class GraphEmailServiceIntegrationTest {

    @Autowired
    private EmailService graphEmailService;

    @Test
    void testSendEmail_RealIntegration() {
        String fromEmail = "vms@statusneo.com";
        String toEmail = "akshay.mathur@statusneo.com";
        String subject = "Integration Test Subject";
        String body = "This is a test email from GraphEmailService integration test.";

        boolean result = graphEmailService.sendEmail(fromEmail, toEmail, subject, body);
        assertTrue(result, "Email should be sent successfully in integration environment");
    }
} 