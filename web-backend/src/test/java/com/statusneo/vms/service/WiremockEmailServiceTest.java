package com.statusneo.vms.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.statusneo.vms.TestcontainersConfiguration;
import com.statusneo.vms.cache.EmployeeNameCache;
import com.statusneo.vms.config.TestRestTemplateConfig;
import com.statusneo.vms.model.Email;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest(classes = {WiremockMailServiceImpl.class, TestRestTemplateConfig.class})
@ActiveProfiles("test")
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