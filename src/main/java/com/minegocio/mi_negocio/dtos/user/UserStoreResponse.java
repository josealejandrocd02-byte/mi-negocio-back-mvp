package com.minegocio.mi_negocio.dtos.user;

import java.util.UUID;

import com.minegocio.mi_negocio.emus.StoreRole;

public class UserStoreResponse {

    private final UUID storeId;
    private final String storeName;
    private final StoreRole role;
    private final boolean active; // acceso del usuario a ESA sucursal

    public UserStoreResponse(UUID storeId, String storeName, StoreRole role, boolean active) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.role = role;
        this.active = active;
    }

    public UUID getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public StoreRole getRole() { return role; }
    public boolean isActive() { return active; }
}
