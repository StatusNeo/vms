package com.statusneo.vms.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.statusneo.vms.dto.DirectorySyncResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 0)
public class GraphDirectoryServiceTest {

    @InjectWireMock
    private static WireMockServer wiremock;

    @Autowired
    private GraphDirectoryService graphDirectoryService;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("graph.base-url", () -> "http://localhost:" + wiremock.port());
        registry.add("azure.tenant-id", () -> "dummy-tenant");
        registry.add("azure.client-id", () -> "dummy-client");
        registry.add("azure.client-secret", () -> "dummy-secret");
    }

    @BeforeEach
    void setupStubs() {
        wiremock.stubFor(post(urlPathMatching("/test.onmicrosoft.com/oauth2/v2.0/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"mock-access-token\"}")));

        wiremock.stubFor(get(urlEqualTo("/v1.0/users/delta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
            {
              "value": [
                {
                  "id": "user-123",
                  "displayName": "Mock User",
                  "mail": "mock.user@example.com",
                  "userPrincipalName": "mock.user@domain.com",
                  "accountEnabled": true
                }
              ],
              "@odata.deltaLink": "https://graph.microsoft.com/v1.0/users/delta?$deltatoken=abc123"
            }
            """)));
    }

    @Test
    void shouldSyncUsersSuccessfully() {
        DirectorySyncResult result = graphDirectoryService.syncAllUsersToEmployees();
        assertEquals(1, result.upserts());
        assertNotNull(result.deltaLink());
    }
}