package com.minegocio.mi_negocio.dtos.store;

import com.minegocio.mi_negocio.emus.StoreStatus;

import jakarta.validation.constraints.NotNull;

public class StoreStatusRequest {

    @NotNull
    private StoreStatus status; // ACTIVE, SUSPENDED, CLOSED

    public StoreStatus getStatus() { return status; }
    public void setStatus(StoreStatus status) { this.status = status; }
}
