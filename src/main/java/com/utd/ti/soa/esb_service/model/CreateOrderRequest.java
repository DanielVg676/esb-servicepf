package com.utd.ti.soa.esb_service.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequest {
    private double amount;
    private String currency;
    private int userId;

    public CreateOrderRequest(double amount, String currency, int userId) {
        this.amount = amount;
        this.currency = currency;
        this.userId = userId;
    }
}
