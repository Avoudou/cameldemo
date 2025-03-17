package com.example.cameldemo.model;

public class EncryptedPayment {
    private final String encryptedCardNumber;
    private final double amount;

    public EncryptedPayment(String encryptedCardNumber, double amount) {
        this.encryptedCardNumber = encryptedCardNumber;
        this.amount = amount;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public double getAmount() {
        return amount;
    }


}
