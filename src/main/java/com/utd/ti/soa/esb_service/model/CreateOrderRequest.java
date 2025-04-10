package com.utd.ti.soa.esb_service.model;

public class CreateOrderRequest {

    private double amount;
    private String currency;
    private int userId;

    // Constructor
    public CreateOrderRequest(double amount, String currency, int userId) {
        this.amount = amount;
        this.currency = currency;
        this.userId = userId;
    }

    // Getters and Setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
