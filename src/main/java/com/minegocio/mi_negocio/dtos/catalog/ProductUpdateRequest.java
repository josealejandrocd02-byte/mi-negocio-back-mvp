package com.minegocio.mi_negocio.dtos.catalog;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ProductUpdateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    // Categoria de la MISMA sucursal (opcional; null = sin categoria)
    private UUID categoryId;

    @NotNull
    @Positive
    private BigDecimal price;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
