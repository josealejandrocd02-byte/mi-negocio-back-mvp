package com.minegocio.mi_negocio.dtos.order;

import com.minegocio.mi_negocio.emus.OrderStatus;

import jakarta.validation.constraints.NotNull;

public class OrderStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private OrderStatus status; // PENDING, PREPARING, READY, DELIVERED, CANCELLED

    public OrderStatusRequest() {
    }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}