package com.statusneo.vms.service;

import com.statusneo.vms.controller.VisitorController;
import com.statusneo.vms.model.Email;
import com.statusneo.vms.model.Attachment;
import com.statusneo.vms.repository.EmployeeRepository;
import com.statusneo.vms.repository.OtpRepository;
import com.statusneo.vms.repository.VisitRepository;
import com.statusneo.vms.repository.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
@Import(GraphEmailServiceTest.class)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
class GraphEmailServiceIntegrationTest {

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @MockitoBean
    private VisitRepository visitRepository;

    @MockitoBean
    private VisitorController visitorController;

    @MockitoBean
    private OtpRepository otpRepository;

    @MockitoBean
    private VisitorRepository visitorRepository;

    @Autowired
    private RestClient restClient;

    @Autowired
    @Qualifier("emailService")
    private EmailService graphEmailService;

    @BeforeEach
    void setUp() {
        graphEmailService = new GraphEmailService(authorizedClientManager, restClient);
    }



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