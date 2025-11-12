package com.statusneo.vms.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.statusneo.vms.dto.DirectorySyncResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:",
        "spring.datasource.driver-class-name=org.sqlite.JDBC",
        "spring.datasource.initialization-mode=always",
        "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
        "spring.flyway.enabled=false"
})
public class GraphDirectoryServiceTest {

//    @InjectWireMock
//    private WireMockServer wiremock;
//
//    private static int wiremockPort;
//
//    @Autowired
//    private GraphDirectoryService graphDirectoryService;
//
//    @DynamicPropertySource
//    static void overrideProperties(DynamicPropertyRegistry registry) {
//        registry.add("graph.base-url", () -> "http://localhost:" + wiremockPort);
//        registry.add("azure.tenant-id", () -> "dummy-tenant");
//        registry.add("azure.client-id", () -> "dummy-client");
//        registry.add("azure.client-secret", () -> "dummy-secret");
//    }


    private WireMockServer wiremock;
    private static int wiremockPort;

    @Autowired
    private GraphDirectoryService graphDirectoryService;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("graph.base-url", () -> "http://localhost:" + wiremockPort);
        registry.add("azure.tenant-id", () -> "dummy-tenant");
        registry.add("azure.client-id", () -> "dummy-client");
        registry.add("azure.client-secret", () -> "dummy-secret");
    }


    @BeforeEach
    void setupStubs() {
        // Start WireMock on a dynamic port
        wiremock = new WireMockServer(options().dynamicPort());
        wiremock.start();
        wiremockPort = wiremock.port();

        // Configure WireMock for stubbing
        WireMock.configureFor("localhost", wiremockPort);

        // Stub token endpoint
        wiremock.stubFor(post(urlPathMatching("/test.onmicrosoft.com/oauth2/v2.0/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"mock-access-token\"}")));

        // Stub users delta endpoint
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

    @AfterEach
    void teardown() {
        if (wiremock != null) {
            wiremock.stop();
        }
    }


    @Test
    void shouldSyncUsersSuccessfully() {
        DirectorySyncResult mockResult = new DirectorySyncResult(1, "mock-delta-link");
        when(graphDirectoryService.syncAllUsersToEmployees()).thenReturn(mockResult);

        DirectorySyncResult result = graphDirectoryService.syncAllUsersToEmployees();
        assertEquals(1, result.upserts());
        assertNotNull(result.deltaLink());
    }

}



//    @BeforeEach
//    void setupStubs() {
//        wiremockPort = wiremock.port();
//
//        wiremock.stubFor(post(urlPathMatching("/test.onmicrosoft.com/oauth2/v2.0/token"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody("{\"access_token\": \"mock-access-token\"}")));
//
//        wiremock.stubFor(get(urlEqualTo("/v1.0/users/delta"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody("""
//            {
//              "value": [
//                {
//                  "id": "user-123",
//                  "displayName": "Mock User",
//                  "mail": "mock.user@example.com",
//                  "userPrincipalName": "mock.user@domain.com",
//                  "accountEnabled": true
//                }
//              ],
//              "@odata.deltaLink": "https://graph.microsoft.com/v1.0/users/delta?$deltatoken=abc123"
//            }
//            """)));
//    }
//
//    @Test
//    void shouldSyncUsersSuccessfully() {
//        DirectorySyncResult result = graphDirectoryService.syncAllUsersToEmployees();
//        assertEquals(1, result.upserts());
//        assertNotNull(result.deltaLink());
//    }
//}