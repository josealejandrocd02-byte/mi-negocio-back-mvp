package com.minegocio.mi_negocio.dtos.sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SaleItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private Instant createdAt; // <-- Añadido para la fecha y hora

    // Constructor completo
    public SaleItemResponse(UUID id, UUID productId, String productName, int quantity, BigDecimal unitPrice, Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}