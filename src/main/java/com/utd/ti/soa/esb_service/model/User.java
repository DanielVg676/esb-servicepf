package com.utd.ti.soa.esb_service.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class User {
    private String username;
    private String phone;
    private String password;
}

public class CreateOrderRequest {
    private Long clientId;
    private List<Long> productIds;
    private Double totalAmount;
}
