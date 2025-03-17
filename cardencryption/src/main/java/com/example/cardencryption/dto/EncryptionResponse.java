package com.example.cardencryption.dto;

public class EncryptionResponse {
    private String cardNumber;

    public EncryptionResponse() {}

    public EncryptionResponse(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}

