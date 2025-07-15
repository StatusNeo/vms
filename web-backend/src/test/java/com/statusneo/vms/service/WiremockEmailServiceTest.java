    package com.statusneo.vms.service;

    import static org.junit.jupiter.api.Assertions.assertTrue;

    import com.statusneo.vms.TestcontainersConfiguration;
    import com.statusneo.vms.model.Email;
    import org.junit.jupiter.api.*;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.context.annotation.Import;
    import org.springframework.test.context.ActiveProfiles;

    @SpringBootTest
    @Import(TestcontainersConfiguration.class)
    @ActiveProfiles("test")
    public class WiremockEmailServiceTest {

        @Autowired
        private WiremockMailServiceImpl wiremockMailService;

        @Test
        void testSendEmail() {
            String fromEmail = "sender@sender.com";
            String toEmail = "recipient@recipient.com";
            String subject = "Integration Test Subject";
            String body = "This is a test email from GraphEmailService integration test.";

            boolean result = wiremockMailService.sendEmail(Email.of(fromEmail, toEmail, subject, body));
            assertTrue(result, "Email should be sent successfully in integration environment");
        }
    }
