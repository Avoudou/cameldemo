package com.example.cameldemo.controllers;

import com.example.cameldemo.model.CardBalance;
import com.example.cameldemo.repository.CardBalanceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardBalanceTestController {

    private final CardBalanceRepository repository;

    public CardBalanceTestController(CardBalanceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CardBalance> getAll() {
        return repository.findAll();
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAll() {
        repository.deleteAll();
        return ResponseEntity.ok("All card balances deleted");
    }

    @PostMapping
    public ResponseEntity<CardBalance> update(@RequestBody CardBalance card) {
        CardBalance existing = repository.findByCardNumber(card.getCardNumber());
        if (existing != null) {
            existing.setBalance(card.getBalance());
            return ResponseEntity.ok(repository.save(existing));
        } else {
            return ResponseEntity.ok(repository.save(card));
        }
    }
}
