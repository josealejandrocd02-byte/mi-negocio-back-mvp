package com.minegocio.mi_negocio.dtos.catalog;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ProductResponse {

    private final UUID id;
    private final String name;
    private final UUID categoryId;
    private final String categoryName;
    private final BigDecimal price;
    private final boolean active;
    private final String imageUrl;
    private final Instant createdAt;

    public ProductResponse(UUID id, String name, UUID categoryId, String categoryName,
                           BigDecimal price, boolean active, String imageUrl, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.active = active;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public BigDecimal getPrice() { return price; }
    public boolean isActive() { return active; }
    public String getImageUrl() { return imageUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
