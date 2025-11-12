package com.statusneo.vms.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.statusneo.vms.TestcontainersConfiguration;
import com.statusneo.vms.VmsApplication;
import com.statusneo.vms.config.TestRestTemplateConfig;
import com.statusneo.vms.model.Visitor;
import com.statusneo.vms.repository.VisitorRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@SpringBootTest(classes = {VmsApplication.class, TestRestTemplateConfig.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.datasource.url=jdbc:sqlite::memory:",
        "mail.url=http://localhost:8081/v1.0/users/%s/sendMail"

})

class ExcelServiceITest {

    @Autowired
    private ExcelService excelService;


    private static WireMockServer wireMockServer;


    @Autowired
    private VisitorRepository visitorRepository;


    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);

        wireMockServer.stubFor(WireMock.post(WireMock.urlMatching("/v1\\.0/users/.+/sendMail"))
                .willReturn(WireMock.aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\": \"Email accepted by mock server\"}")));
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }



    @BeforeEach
    void setUp() {
        // Clean up and insert test visitors
        visitorRepository.deleteAll();
        Visitor v1 = new Visitor(null, "Alice Smith", "1112223333", "alice@example.com", "123 Main St");
        Visitor v2 = new Visitor(null, "Bob Jones", "4445556666", "bob@example.com", "456 Oak Ave");
        Visitor v3 = new Visitor(null, "Carol White", "7778889999", "carol@example.com", "789 Pine Rd");
        visitorRepository.saveAll(List.of(v1, v2, v3));
    }

    @Test
    @Transactional
    void testSendVisitorReport_GeneratesAndSendsExcel() {
        // This will generate the Excel and send it via EmailService (GraphEmailService in prod)
        assertDoesNotThrow(() -> excelService.sendVisitorReport(), "sendVisitorReport should not throw");
    }
}
