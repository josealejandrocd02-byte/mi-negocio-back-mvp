package com.minegocio.mi_negocio.dtos.saleitem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SaleItemCreateRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private UUID productId;

    @Min(value = 1, message = "La cantidad minima es 1")
    private int quantity;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}