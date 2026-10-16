package com.minegocio.mi_negocio.dtos.sale;

import com.minegocio.mi_negocio.emus.SaleStatus;

import jakarta.validation.constraints.NotNull;

public class SaleStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private SaleStatus status;

    public SaleStatus getStatus() { return status; }
    public void setStatus(SaleStatus status) { this.status = status; }
}
