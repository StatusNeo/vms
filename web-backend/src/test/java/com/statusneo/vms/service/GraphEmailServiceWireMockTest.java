package com.statusneo.vms.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.statusneo.vms.model.Email;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GraphEmailServiceWireMockTest {

    private static final WireMockServer wireMockServer;

    static {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"dummy-access-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}")));

        stubFor(post(urlMatching("/v1.0/users/.*/sendMail"))
                .willReturn(aResponse().withStatus(202)));
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.client.provider.azure.token-uri",
                () -> "http://localhost:" + wireMockServer.port() + "/oauth2/token");
        registry.add("graph.api.base-url",
                () -> "http://localhost:" + wireMockServer.port() + "/v1.0");
    }

    @Autowired
    private GraphEmailService graphEmailService;

    @Test
    void testSendEmail_Success() {
        boolean result = graphEmailService.sendEmail(
                Email.of("system-user@example.com", "recipient@example.com", "Subject", "Body")
        );

        assertTrue(result, "Email should be sent successfully");

        System.out.println("==== WireMock Recorded Requests ====");
        wireMockServer.getAllServeEvents().forEach(event -> {
            System.out.println("Request URL: " + event.getRequest().getUrl());
            System.out.println("Headers: " + event.getRequest().getHeaders());
            System.out.println("Body: " + event.getRequest().getBodyAsString());
        });

        WireMock.configureFor("localhost", wireMockServer.port());

        verify(postRequestedFor(urlMatching("/v1.0/users/.*/sendMail"))
                .withHeader("Authorization", matching("Bearer .*")));
    }


    @Test
    void testSendEmail_FailureFromGraphAPI() {
        // Override stub for failure
        stubFor(post(urlMatching("/v1.0/users/.*/sendMail"))
                .willReturn(aResponse().withStatus(400)));

        Exception exception = assertThrows(HttpClientErrorException.class, () ->
                graphEmailService.sendEmail(
                        Email.of("system-user@example.com", "recipient@example.com", "Subject", "Body")
                )
        );

        assertEquals(400, ((HttpClientErrorException) exception).getStatusCode().value(),
                "Should throw 400 Bad Request");
    }

    @Test
    void testSendEmail_TokenEndpointFailure() {
        stubFor(post(urlEqualTo("/oauth2/token"))
                .willReturn(aResponse().withStatus(500)));

        Exception exception = assertThrows(HttpClientErrorException.class, () ->
                graphEmailService.sendEmail(
                        Email.of("system-user@example.com", "recipient@example.com", "Subject", "Body")
                )
        );

        assertEquals(400, ((HttpClientErrorException) exception).getStatusCode().value(),
                "Should fail due to invalid token causing bad request");
    }

    @AfterAll
    void stopWireMock() {
        wireMockServer.stop();
    }
}
