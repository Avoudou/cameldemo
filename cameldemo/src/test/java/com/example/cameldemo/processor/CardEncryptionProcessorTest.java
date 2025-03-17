package com.example.cameldemo.processor;

import com.example.cameldemo.model.EncryptedPayment;
import com.example.cameldemo.model.Payment;
import com.example.cameldemo.wiremock.DemoWiremockAssistant;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CardEncryptionProcessorTest extends DemoWiremockAssistant {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private CardEncryptionProcessor processor;

    @BeforeEach
    void setupMock() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlPathEqualTo("/api/encrypt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("card-encryption-transformer")));
    }
    @AfterEach
    void resetStubs(){
        wireMockServer.resetAll();
    }
    @Test
    void testEncryptionProcessor() {
        Payment payment = new Payment("4111111111111111", 20.0);

        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(payment);

        processor.process(exchange);

        EncryptedPayment result = exchange.getMessage().getBody(EncryptedPayment.class);
        assertNotNull(result);
        assertTrue(result.getEncryptedCardNumber().startsWith("mockEncrypted_"));
        assertEquals(20.0, result.getAmount());
    }

    @Test
    void testEncryptionFailsWithServerError() {
        wireMockServer.stubFor(post(urlPathEqualTo("/api/encrypt"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        Payment payment = new Payment("4111111111111111", 20.0);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(payment);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processor.process(exchange);
        });

        Throwable current = exception;
        boolean foundStatus500 = false;

        while (current != null) {
            System.out.println("Cause: " + current.getClass() + " - " + current.getMessage());
            if (current.getMessage() != null && current.getMessage().contains("500")) {
                foundStatus500 = true;
                break;
            }
            current = current.getCause();
        }

        assertTrue(foundStatus500, "Expected exception to contain HTTP 500 status");
    }

    @Test
    void testEncryptionFailsWithMissingCardNumber() {
        wireMockServer.stubFor(post(urlPathEqualTo("/api/encrypt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"someOtherField\": \"value\"}")));

        Payment payment = new Payment("4111111111111111", 20.0);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(payment);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processor.process(exchange);
        });

        String message = exception.getMessage();
        System.out.println("Exception message: " + message);
        assertTrue(message.contains("missing card number"));
    }

    @Test
    void testEncryptionTimeout() {
        wireMockServer.stubFor(post(urlPathEqualTo("/api/encrypt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(4000)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"cardNumber\": \"mockEncrypted_4111111111111111\"}")));

        Payment payment = new Payment("4111111111111111", 20.0);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(payment);

        long start = System.currentTimeMillis();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            processor.process(exchange);
        });

        long duration = System.currentTimeMillis() - start;
        System.out.println("Request duration: " + duration + "ms");

        // Assert request took ~3s or more and failed
        assertTrue(duration >= 3000 && duration < 6000, "Expected the call to timeout around 3s");
        assertTrue(exception.getMessage().toLowerCase().contains("encryption failed"), "Expected a timeout-related failure");
    }
}
