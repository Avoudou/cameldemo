package com.example.cameldemo.processor;

import com.example.cameldemo.dto.EncryptionResponse;
import com.example.cameldemo.model.EncryptedPayment;
import com.example.cameldemo.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.camel.*;
import org.apache.camel.component.http.HttpComponent;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CardEncryptionProcessor implements Processor {

    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${encryption.service.url}")
    private String encryptionServiceUrl;

    private final CamelContext camelContext;

    public CardEncryptionProcessor(ProducerTemplate producerTemplate, CamelContext camelContext) {
        this.producerTemplate = producerTemplate;
        this.camelContext = camelContext;
    }

    @PostConstruct
    public void configureHttpClient5() {
        try {
            HttpComponent httpComponent = camelContext.getComponent("http", HttpComponent.class);

            httpComponent.setHttpClientConfigurer(clientBuilder -> {
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(3000))
                        .setResponseTimeout(Timeout.ofMilliseconds(3000))
                        .build();

                clientBuilder.setDefaultRequestConfig(requestConfig);
            });

        } catch (Exception e) {
            throw new RuntimeException("Failed to configure HTTP timeouts", e);
        }
    }
    @Override
    public void process(Exchange exchange) {
        try {
            Payment payment = exchange.getIn().getBody(Payment.class);
            if (payment == null || payment.getCardNumber() == null) {
                throw new IllegalArgumentException("Invalid payment data");
            }

            Map<String, String> request = new HashMap<>();
            request.put("cardNumber", payment.getCardNumber());

            String jsonBody = objectMapper.writeValueAsString(request);

            Map<String, Object> headers = new HashMap<>();
            headers.put(Exchange.HTTP_METHOD, "POST");
            headers.put(Exchange.CONTENT_TYPE, "application/json");

            String url = encryptionServiceUrl + "?bridgeEndpoint=true";

            String responseString = producerTemplate.requestBodyAndHeaders(
                    url, jsonBody, headers, String.class
            );

//            System.out.println("RESPONSE BODY: " + responseString);

            EncryptionResponse responseObj = objectMapper.readValue(responseString, EncryptionResponse.class);

            if (responseObj.getCardNumber() == null) {
                throw new RuntimeException("Encryption failed: missing card number in response");
            }

            EncryptedPayment encryptedPayment = new EncryptedPayment(responseObj.getCardNumber(), payment.getAmount());
            exchange.getMessage().setBody(encryptedPayment);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }
}
