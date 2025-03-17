package com.example.cameldemo.repository;
import com.example.cameldemo.model.CardBalance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardBalanceRepository extends MongoRepository<CardBalance, String> {

    CardBalance findByCardNumber(String cardNumber);
}
