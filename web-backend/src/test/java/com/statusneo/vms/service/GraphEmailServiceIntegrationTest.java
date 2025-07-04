package com.statusneo.vms.service;

import com.statusneo.vms.TestcontainersConfiguration;
import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Attachment;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GraphEmailServiceIntegrationTest {

    @Autowired
    private EmailService graphEmailService;

    /**
     * This test will attempt to send a real email using the configuration in your test profile.
     * You may want to disable it by default to avoid sending emails during every test run.
     * Remove @Disabled to enable real integration testing.
     */
    @Test
    @Disabled("Enable this test with real credentials and configuration for full integration testing.")
    void testSendEmail_RealIntegration() {
        String fromEmail = "sender@sender.com";
        String toEmail = "recipient@recipient.com";
        String subject = "Integration Test Subject";
        String body = "This is a test email from GraphEmailService integration test.";

        // Add a test attachment
        byte[] fileData = "Hello, this is a test attachment.".getBytes();
        Attachment attachment = new Attachment("test.txt", fileData, "text/plain");

        boolean result = graphEmailService.sendEmail(
            new Email(
                fromEmail,
                List.of(toEmail),
                subject,
                body,
                List.of(attachment)
            )
        );
        assertTrue(result, "Email should be sent successfully in integration environment");
    }
} 