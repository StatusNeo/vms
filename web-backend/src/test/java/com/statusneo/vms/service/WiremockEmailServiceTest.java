    package com.statusneo.vms.service;

    import static com.github.tomakehurst.wiremock.client.WireMock.*;
    import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
    import static org.junit.jupiter.api.Assertions.assertTrue;

    import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
    import com.statusneo.vms.TestcontainersConfiguration;
    import com.statusneo.vms.model.Email;
    import org.junit.jupiter.api.*;
    import org.junit.jupiter.api.extension.RegisterExtension;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.context.annotation.Import;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.context.DynamicPropertyRegistry;
    import org.springframework.test.context.DynamicPropertySource;

    @SpringBootTest
    @Import(TestcontainersConfiguration.class)
    @ActiveProfiles("test")
    public class WiremockEmailServiceTest {


        @RegisterExtension
        static WireMockExtension wiremock = WireMockExtension.newInstance()
                .options(wireMockConfig().dynamicPort())
                .build();

        @DynamicPropertySource
        static void overrideMailUrl(DynamicPropertyRegistry registry) {
            registry.add("mail.url", () -> "http://localhost:" + wiremock.getPort() + "/v1.0/users/%s/sendMail");
        }


        @Autowired
        private WiremockMailServiceImpl wiremockMailService;

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
