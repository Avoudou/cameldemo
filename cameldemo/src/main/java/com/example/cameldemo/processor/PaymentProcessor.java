package com.example.cameldemo.processor;

import com.example.cameldemo.model.CardBalance;
import com.example.cameldemo.model.EncryptedPayment;
import com.example.cameldemo.repository.CardBalanceRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class PaymentProcessor implements Processor {

    private final CardBalanceRepository repo;

    public PaymentProcessor(CardBalanceRepository repo) {
        this.repo = repo;
    }

    @Override
    public void process(Exchange exchange) {
        EncryptedPayment encryptedPayment = exchange.getIn().getBody(EncryptedPayment.class);

        CardBalance card = repo.findByCardNumber(encryptedPayment.getEncryptedCardNumber());
        if (card == null) {
            throw new RuntimeException("Card not found: " + encryptedPayment.getEncryptedCardNumber());
        }

        if (encryptedPayment.getAmount() <= 0) {
            throw new RuntimeException("Invalid payment amount: " + encryptedPayment.getAmount());
        }

        if (card.getBalance() < encryptedPayment.getAmount()) {
            throw new RuntimeException("Insufficient balance for card: " + encryptedPayment.getEncryptedCardNumber());
        }

        card.setBalance(card.getBalance() - encryptedPayment.getAmount());
        repo.save(card);
    }
}
