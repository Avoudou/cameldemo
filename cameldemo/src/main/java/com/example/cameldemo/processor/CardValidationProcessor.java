package com.example.cameldemo.processor;

import com.example.cameldemo.model.Payment;
import com.example.cameldemo.util.CardUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class CardValidationProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        Payment payment = exchange.getIn().getBody(Payment.class);
        String cardNumber = payment.getCardNumber();

        if (!CardUtils.isValidCard(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number: " + cardNumber);
        }
    }
}
