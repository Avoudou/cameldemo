package com.example.cameldemo.model;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",", skipFirstLine = true)
public class Payment {

    @DataField(pos = 1)
    private String cardNumber;

    @DataField(pos = 2)
    private double amount;

    public Payment(String cardNumber, double amount) {
        this.cardNumber = cardNumber;
        this.amount = amount;
    }

    public Payment() {
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
