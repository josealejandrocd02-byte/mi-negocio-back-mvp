package com.minegocio.mi_negocio.dtos.user;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    // Opcional: sucursales a las que se liga el usuario al crearlo (1, 2, 3...)
    @Valid
    private List<StoreAssignmentRequest> stores;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<StoreAssignmentRequest> getStores() { return stores; }
    public void setStores(List<StoreAssignmentRequest> stores) { this.stores = stores; }
}
