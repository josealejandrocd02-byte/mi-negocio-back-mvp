package com.minegocio.mi_negocio.dtos.store;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

// Cambiar dueno o vincular un STORE_ADMIN: solo necesita el id del usuario
public class StoreUserLinkRequest {

    @NotNull
    private UUID userId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
