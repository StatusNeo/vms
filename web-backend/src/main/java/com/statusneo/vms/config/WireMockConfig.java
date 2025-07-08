package com.statusneo.vms.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


@Configuration
@Profile({"dev", "test"})
public class WireMockConfig {

    private WireMockServer wireMockServer;

    @PostConstruct
    public void startMockServer() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();

        wireMockServer.stubFor(
                post(urlMatching("/v1.0/.*/sendMail"))
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
