package com.example.cameldemo.integration;

import com.example.cameldemo.model.CardBalance;
import com.example.cameldemo.repository.CardBalanceRepository;
import com.example.cameldemo.wiremock.DemoWiremockAssistant;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentRouteIntegrationTest extends DemoWiremockAssistant {

    @Autowired
    private CardBalanceRepository repository;

    private static final String CARD_1 = "4111111111111111";
    private static final String ENCRYPTED_CARD_1 = "mockEncrypted_" + CARD_1;

    private static final Path PAYMENT_FILE = Path.of("input_payments/test-payment.csv");

    @BeforeEach
    void setup() {
        repository.deleteAll();
        repository.save(new CardBalance(ENCRYPTED_CARD_1, 100.0));

        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlPathEqualTo("/api/encrypt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withTransformers("card-encryption-transformer")));
    }

    @Test
    void testValidPaymentDecreasesBalance() throws Exception {
        writeCsv("cardNumber,amount\n" + CARD_1 + ",30.0\n");
        waitForRouteToComplete();
        CardBalance updated = repository.findByCardNumber(ENCRYPTED_CARD_1);
        assertNotNull(updated);
        assertEquals(70.0, updated.getBalance());
    }

    @Test
    void testInvalidCardIsRejected() throws Exception {
        writeCsv("cardNumber,amount\ninvalidCard,30.0\n");
        waitForRouteToComplete();
        assertNull(repository.findByCardNumber("mockEncrypted_invalidCard"));
    }

    @Test
    void testExactBalancePayment() throws Exception {
        writeCsv("cardNumber,amount\n" + CARD_1 + ",100.0\n");
        waitForRouteToComplete();
        CardBalance updated = repository.findByCardNumber(ENCRYPTED_CARD_1);
        assertNotNull(updated);
        assertEquals(0.0, updated.getBalance());
    }

    @Test
    void testInsufficientBalance() throws Exception {
        writeCsv("cardNumber,amount\n" + CARD_1 + ",150.0\n");
        waitForRouteToComplete();
        CardBalance updated = repository.findByCardNumber(ENCRYPTED_CARD_1);
        assertNotNull(updated);
        assertEquals(100.0, updated.getBalance());
    }

    @Test
    void testMixedValidAndInvalidPayments() throws Exception {
        writeCsv("cardNumber,amount\n" + CARD_1 + ",50.0\ninvalidCard,25.0\n" + CARD_1 + ",25.0\n");
        waitForRouteToComplete();
        CardBalance updated = repository.findByCardNumber(ENCRYPTED_CARD_1);
        assertNotNull(updated);
        assertEquals(25.0, updated.getBalance());
        assertNull(repository.findByCardNumber("mockEncrypted_invalidCard"));
    }

    @Test
    void testZeroAndNegativeAmountsAreRejected() throws Exception {
        writeCsv("cardNumber,amount\n" + CARD_1 + ",0.0\n");
        waitForRouteToComplete();
        CardBalance unchanged1 = repository.findByCardNumber(ENCRYPTED_CARD_1);
        assertNotNull(unchanged1);
        assertEquals(100.0, unchanged1.getBalance());

        writeCsv("cardNumber,amount\n" + CARD_1 + ",-10.0\n");
        waitForRouteToComplete();
        CardBalance unchanged2 = repository.findByCardNumber(ENCRYPTED_CARD_1);
        assertNotNull(unchanged2);
        assertEquals(100.0, unchanged2.getBalance());
    }

    private void writeCsv(String content) throws Exception {
        Files.createDirectories(PAYMENT_FILE.getParent());
        Files.writeString(PAYMENT_FILE, content);
    }

    private void waitForRouteToComplete() throws InterruptedException {
        Thread.sleep(1000);
    }
}
