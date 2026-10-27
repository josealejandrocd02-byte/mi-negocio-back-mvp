package com.minegocio.mi_negocio.dtos.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.minegocio.mi_negocio.emus.OrderStatus;

public class OrderResponse {

    private final UUID id;
    private final UUID storeId;
    private final String storeName;
    private final UUID createdByUserId;
    private final String createdByName;
    private final String customerName;
    private final String customerPhone;
    private final String deliveryAddress;
    private final String notes;
    private final OrderStatus status;
    private final BigDecimal total;
    private final Instant createdAt;
    private final List<OrderItemResponse> items;

    public OrderResponse(UUID id, UUID storeId, String storeName,
                         UUID createdByUserId, String createdByName,
                         String customerName, String customerPhone,
                         String deliveryAddress, String notes,
                         OrderStatus status, BigDecimal total,
                         Instant createdAt, List<OrderItemResponse> items) {
        this.id = id;
        this.storeId = storeId;
        this.storeName = storeName;
        this.createdByUserId = createdByUserId;
        this.createdByName = createdByName;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.deliveryAddress = deliveryAddress;
        this.notes = notes;
        this.status = status;
        this.total = total;
        this.createdAt = createdAt;
        this.items = items;
    }

    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public UUID getCreatedByUserId() { return createdByUserId; }
    public String getCreatedByName() { return createdByName; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getNotes() { return notes; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItemResponse> getItems() { return items; }
}