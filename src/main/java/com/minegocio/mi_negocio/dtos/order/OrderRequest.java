package com.minegocio.mi_negocio.dtos.order;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public class OrderRequest {

    @Size(max = 150)
    private String customerName;

    @Size(max = 30)
    private String customerPhone;

    private String deliveryAddress;

    private String notes;

    @Valid
    private List<OrderItemRequest> items;

    public OrderRequest() {
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}