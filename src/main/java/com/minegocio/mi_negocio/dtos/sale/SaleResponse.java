package com.minegocio.mi_negocio.dtos.sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.minegocio.mi_negocio.emus.SaleStatus;

public class SaleResponse {

    private final UUID id;
    private final UUID storeId;
    private final String storeName;
    private final UUID orderId;
    private final UUID soldById;
    private final String soldByName;
    private final BigDecimal total;
    private final SaleStatus status;
    private final Instant createdAt;
    private final List<SaleItemResponse> items;

    public SaleResponse(UUID id, UUID storeId, String storeName, UUID orderId,
                        UUID soldById, String soldByName, BigDecimal total,
                        SaleStatus status, Instant createdAt, List<SaleItemResponse> items) {
        this.id = id;
        this.storeId = storeId;
        this.storeName = storeName;
        this.orderId = orderId;
        this.soldById = soldById;
        this.soldByName = soldByName;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
    }

    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public UUID getOrderId() { return orderId; }
    public UUID getSoldById() { return soldById; }
    public String getSoldByName() { return soldByName; }
    public BigDecimal getTotal() { return total; }
    public SaleStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<SaleItemResponse> getItems() { return items; }
}
