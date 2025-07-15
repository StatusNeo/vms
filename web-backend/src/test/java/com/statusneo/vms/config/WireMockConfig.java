package com.statusneo.vms.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

@Configuration
@Profile({"default", "dev", "test"})
public class WireMockConfig {
    private static final Logger logger = LoggerFactory.getLogger(WireMockConfig.class);
    private WireMockServer wireMockServer;

    @PostConstruct
    public void startMockServer() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();

        logger.info("WireMock server started at port {}", wireMockServer.port());
        wireMockServer.stubFor(
                post(urlMatching("/v1\\.0/users/[^/]+@[^/]+/sendMail"))
                        .willReturn(
                                aResponse()
                                        .withStatus(202)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"message\": \"Email accepted by mock server\"}")
                        )
        );


    }

    @PreDestroy
    public void stopMockServer() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
