package com.minegocio.mi_negocio.dtos.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StoreUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    private String address;

    @Size(max = 30)
    private String phone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
