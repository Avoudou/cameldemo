package com.example.cameldemo;

import com.example.cameldemo.model.CardBalance;
import com.example.cameldemo.repository.CardBalanceRepository;
import com.example.cameldemo.wiremock.DemoWiremockAssistant;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentRouteFlowDemoTest extends DemoWiremockAssistant {

    @Autowired
    private CardBalanceRepository repository;

    private static final String CARD_1 = "4111111111111111";
    private static final String CARD_2 = "4242424242424242";

    private static final String ENCRYPTED_CARD_1 = "mockEncrypted_" + CARD_1;
    private static final String ENCRYPTED_CARD_2 = "mockEncrypted_" + CARD_2;

    private static final Path PAYMENT_FILE = Path.of("input_payments/demo-flow.csv");

    @BeforeEach
    void setup() throws Exception {
        repository.deleteAll();

        System.out.println("\n--- Preparing initial test state ---");

        repository.save(new CardBalance(ENCRYPTED_CARD_1, 100.0));
        repository.save(new CardBalance(ENCRYPTED_CARD_2, 50.0));

        System.out.println("Database contents:");
        repository.findAll().forEach(balance ->
                System.out.println("  " + balance.getCardNumber() + " = " + balance.getBalance())
        );

        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlPathEqualTo("/api/encrypt"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withTransformers("card-encryption-transformer")));

        System.out.println("WireMock stub configured for /api/encrypt");
    }

    @Test
    void testDemoFlowWithMultiplePayments() throws Exception {
        System.out.println("\n--- Writing input CSV with 4 payments ---");
        String csvContent = String.join("\n",
                "cardNumber,amount",
                CARD_1 + ",30.0",             // valid
                "invalidCard,25.0",           // invalid
                CARD_2 + ",25.0",             // valid
                CARD_2 + ",-10.0"             // negative, should be ignored
        );
        System.out.println(csvContent);
        writeCsv(csvContent);

        System.out.println("CSV file written to: " + PAYMENT_FILE.toAbsolutePath());
        System.out.println("Waiting for Camel to process the file...");

        waitForRouteToComplete();

        System.out.println("\n--- Verifying database state after processing ---");
        CardBalance balance1 = repository.findByCardNumber(ENCRYPTED_CARD_1);
        CardBalance balance2 = repository.findByCardNumber(ENCRYPTED_CARD_2);
        CardBalance invalid = repository.findByCardNumber("mockEncrypted_invalidCard");

        System.out.println("  " + ENCRYPTED_CARD_1 + " = " + (balance1 != null ? balance1.getBalance() : "null"));
        System.out.println("  " + ENCRYPTED_CARD_2 + " = " + (balance2 != null ? balance2.getBalance() : "null"));
        System.out.println("  mockEncrypted_invalidCard = " + (invalid != null ? invalid.getBalance() : "null"));

        Assertions.assertNotNull(balance1);
        Assertions.assertEquals(70.0, balance1.getBalance());

        Assertions.assertNotNull(balance2);
        Assertions.assertEquals(25.0, balance2.getBalance(), "Negative amount should not affect balance");

        Assertions.assertNull(invalid);
    }

    private void writeCsv(String content) throws Exception {
        Files.createDirectories(PAYMENT_FILE.getParent());
        Files.writeString(PAYMENT_FILE, content);
    }

    private void waitForRouteToComplete() throws InterruptedException {
        Thread.sleep(1500);
    }
}
