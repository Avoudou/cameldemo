package com.example.cameldemo.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class DemoWiremockAssistant {

    protected static WireMockServer wireMockServer;

    @BeforeAll
    static void setupWiremock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(9090)
                .extensions(new CardEncryptionTransformer()));
        wireMockServer.start();
    }

    @AfterAll
    static void teardownWiremock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
