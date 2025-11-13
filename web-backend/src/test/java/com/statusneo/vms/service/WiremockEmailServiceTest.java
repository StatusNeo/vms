package com.statusneo.vms.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.statusneo.vms.config.TestRestTemplateConfig;
import com.statusneo.vms.model.Email;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {WiremockMailServiceImpl.class, TestRestTemplateConfig.class})
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.datasource.url=jdbc:sqlite::memory:",
        "mail.url=http://localhost:8081/v1.0/users/%s/sendMail"
})
public class WiremockEmailServiceTest {

    private WireMockServer wiremock;

    @Autowired
    private WiremockMailServiceImpl wiremockMailService;

    @BeforeEach
    void setup() {
        wiremock = new WireMockServer(8081);
        wiremock.start();
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void teardown() {
        wiremock.stop();
    }

    @Test
    void shouldSendEmailSuccessfully() {
        wiremock.stubFor(post(urlMatching("/v1\\.0/users/[^/]+@[^/]+/sendMail"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Email accepted by mock server\"}")));

        boolean result = wiremockMailService.sendEmail(Email.of(
                "sender@sender.com",
                "recipient@recipient.com",
                "Integration Test Subject",
                "This is a test email."
        ));

        assertTrue(result, "Email should be sent successfully");
    }
}