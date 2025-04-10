package com.utd.ti.soa.esb_service.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Client {
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String birthDate;
    private String address;
    private boolean status;
}
