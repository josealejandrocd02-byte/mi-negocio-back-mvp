package com.minegocio.mi_negocio.dtos.saleitem;

import jakarta.validation.constraints.Min;

public class SaleItemUpdateRequest {

    @Min(value = 1, message = "La cantidad minima es 1")
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}