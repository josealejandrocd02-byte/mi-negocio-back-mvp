package com.minegocio.mi_negocio.dtos.sale;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SaleCreateRequest {

    @NotNull(message = "La sucursal es obligatoria")
    private UUID storeId;

    // Opcional: pedido del que proviene la venta (null = venta directa en mostrador)
    private UUID orderId;

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Size(max = 200, message = "Demasiados productos en la venta")
    @Valid
    private List<SaleItemRequest> items;

    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }
}
