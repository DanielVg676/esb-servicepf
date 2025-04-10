package com.utd.ti.soa.esb_service.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Product {
    private String name;
    private String description;
    private String category;
    private double price;
    private int stock;
    private String brand;
    private boolean status;
}
