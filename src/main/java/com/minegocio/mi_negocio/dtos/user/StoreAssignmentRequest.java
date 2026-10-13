package com.minegocio.mi_negocio.dtos.user;

import java.util.UUID;

import com.minegocio.mi_negocio.emus.StoreRole;

import jakarta.validation.constraints.NotNull;

public class StoreAssignmentRequest {

    @NotNull
    private UUID storeId;

    @NotNull
    private StoreRole role; // STORE_ADMIN o EMPLOYEE

    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }

    public StoreRole getRole() { return role; }
    public void setRole(StoreRole role) { this.role = role; }
}
