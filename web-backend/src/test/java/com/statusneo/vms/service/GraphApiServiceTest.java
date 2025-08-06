package com.statusneo.vms.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.statusneo.vms.TestcontainersConfiguration;
import com.statusneo.vms.model.EmployeeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.time.Instant;
import java.util.List;

@SpringBootTest
@Import({TestcontainersConfiguration.class})
@ActiveProfiles("test")
@EnableWireMock
@WireMockTest(httpPort = 0)
public class GraphApiServiceTest {

    @Autowired
    private GraphApiService graphApiService;

    @InjectWireMock
    private WireMockServer wiremock;

    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OAuth2AuthorizedClientManager authorizedClientManager() {
            return mock(OAuth2AuthorizedClientManager.class);
        }
    }

    @BeforeEach
    public void setupMock() {

        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mocked-access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        OAuth2AuthorizedClient mockClient = mock(OAuth2AuthorizedClient.class);
        when(mockClient.getAccessToken()).thenReturn(token);
        when(authorizedClientManager.authorize(ArgumentMatchers.any()))
                .thenReturn(mockClient);

        wiremock.stubFor(get("/users")
                .withHeader("Authorization", equalTo("Bearer mocked-access-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                    {
                                        "value": [
                                            {
                                                "displayName": "Amit Jangra",
                                                "mail": "amit.jangra@example.com"
                                            },
                                            {
                                                "displayName": "John Doe",
                                                "mail": "john.doe@example.com"
                                            }
                                        ]
                                    }
                                """)));
    }

    @Test
    public void shouldReturnEmployeeList() {
        List<EmployeeDTO> employees = graphApiService.getEmployees();

        assertEquals(2, employees.size());

        assertEquals("Amit Jangra", employees.get(0).getDisplayName());
        assertEquals("amit.jangra@example.com", employees.get(0).getMail());

        assertEquals("John Doe", employees.get(1).getDisplayName());
        assertEquals("john.doe@example.com", employees.get(1).getMail());
    }


}
