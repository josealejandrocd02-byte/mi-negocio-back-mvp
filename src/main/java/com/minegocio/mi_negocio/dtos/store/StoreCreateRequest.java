package com.minegocio.mi_negocio.dtos.store;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StoreCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    private String address;

    @Size(max = 30)
    private String phone;

    // Usuario que sera el dueno (se vincula automaticamente como STORE_ADMIN)
    @NotNull
    private UUID ownerUserId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UUID getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }
}
