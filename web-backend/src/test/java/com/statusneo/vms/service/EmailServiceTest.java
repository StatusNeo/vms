    package com.statusneo.vms.service;

    import static com.github.tomakehurst.wiremock.client.WireMock.*;
    import static org.junit.jupiter.api.Assertions.assertTrue;

    import com.github.tomakehurst.wiremock.WireMockServer;
    import org.junit.jupiter.api.*;
    import org.mockito.InjectMocks;

    public class EmailServiceTest {
        @InjectMocks
        private WiremockMailServiceImpl wiremockMailService;

        private WireMockServer wireMockServer;

        @BeforeEach
        void setup() {
            wireMockServer = new WireMockServer(8089);
            wireMockServer.start();

            wireMockServer.stubFor(post(urlMatching("/v1.0/.*/sendMail"))
                    .willReturn(aResponse()
                            .withStatus(202)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"message\": \"Email accepted by mock server\"}")));
        }

        @AfterEach
        void teardown() {
            wireMockServer.stop();
        }

        @Test
        void testSendEmail() {
            String fromEmail = "sender@sender.com";
            String toEmail = "recipient@recipient.com";
            String subject = "Integration Test Subject";
            String body = "This is a test email from GraphEmailService integration test.";

            boolean result = wiremockMailService.sendEmail(fromEmail, toEmail, subject, body);
            assertTrue(result, "Email should be sent successfully in integration environment");    }
    }
