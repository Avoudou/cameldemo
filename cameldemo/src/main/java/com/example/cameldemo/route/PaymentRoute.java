package com.example.cameldemo.route;

import com.example.cameldemo.configurations.PaymentProperties;
import com.example.cameldemo.model.Payment;
import com.example.cameldemo.processor.CardEncryptionProcessor;
import com.example.cameldemo.processor.CardValidationProcessor;
import com.example.cameldemo.processor.PaymentProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PaymentRoute extends RouteBuilder {

    private final CardValidationProcessor validationProcessor;
    private final PaymentProcessor paymentProcessor;
    private final PaymentProperties paymentProperties;

    private static final BindyCsvDataFormat BINDY_FORMAT = new BindyCsvDataFormat(Payment.class);
    private final CardEncryptionProcessor encryptionProcessor;

    public PaymentRoute(CardValidationProcessor validationProcessor,
                        PaymentProcessor paymentProcessor,
                        CardEncryptionProcessor encryptionProcessor,
                        PaymentProperties paymentProperties) {
        this.validationProcessor = validationProcessor;
        this.paymentProcessor = paymentProcessor;
        this.encryptionProcessor = encryptionProcessor;
        this.paymentProperties = paymentProperties;
    }


    @Override
    public void configure() {
        defineUploadEndpoint();
        definePaymentFileProcessor();
        defineFailedPaymentHandler();
    }

    private void configureRestApi() {
        restConfiguration()
                .component("servlet")
                .contextPath("/");

        rest("/uploadPayments")
                .post()
                .consumes("text/plain")
                .to("direct:uploadPayments");
    }

    private void defineUploadEndpoint() {
        configureRestApi();
        from("direct:uploadPayments")
                .routeId("upload-payment-route")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    String filename = "payments-" + System.currentTimeMillis() + ".csv";

                    Path dir = Paths.get(paymentProperties.getInputFolder());
                    Files.createDirectories(dir);

                    Path filePath = dir.resolve(filename);
                    Files.write(filePath, body.getBytes());

                    exchange.getMessage().setBody("Saved file: " + filePath);
                });
    }

    private void definePaymentFileProcessor() {
        from(String.format("file:%s?noop=false&move=../%s/${file:name}&moveFailed=../%s/${file:name}",
                paymentProperties.getInputFolder(),
                paymentProperties.getProcessedFolder(),
                paymentProperties.getErrorFolder()))
                .routeId("payment-processing")
                .log("Processing file: ${header.CamelFileName}")
                .unmarshal(BINDY_FORMAT)
                .split(body())
                .doTry()
                .process(validationProcessor)
                .process(encryptionProcessor)
                .process(paymentProcessor)
                .log("Processed payment: card=${body.encryptedCardNumber}, amount=${body.amount}")
                .doCatch(Exception.class)
                .log("Failed to process payment: ${exception.message}")
                .to("direct:failed-payments")
                .end()
                .end();
    }

    private void defineFailedPaymentHandler() {
        from("direct:failed-payments")
                .routeId("failed-payment-handler")
                .log("Failed payment routed to handler: ${body}");
    }
}
