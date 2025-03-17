package com.example.cameldemo;


import com.example.cameldemo.model.CardBalance;
import com.example.cameldemo.repository.CardBalanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MongoTest {

    @Autowired
    private CardBalanceRepository repository;

    @Test
    void testInsertAndRead() {

        repository.deleteAll();

        repository.save(new CardBalance("4111111111111111", 100.0));
        repository.save(new CardBalance("5555555555554444", 50.0));

        repository.findAll().forEach(card ->
                System.out.println(card.getCardNumber() + " - Balance: " + card.getBalance()));
    }
}

