package com.minegocio.mi_negocio.dtos.sale;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// Reemplaza los items de la venta; el total se recalcula en el servicio
public class SaleUpdateRequest {

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Size(max = 200, message = "Demasiados productos en la venta")
    @Valid
    private List<SaleItemRequest> items;

    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }
}
