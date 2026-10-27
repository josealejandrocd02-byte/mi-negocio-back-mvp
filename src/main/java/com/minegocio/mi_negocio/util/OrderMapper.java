package com.minegocio.mi_negocio.util;

import java.math.BigDecimal;
import java.util.List;

import com.minegocio.mi_negocio.dtos.order.OrderItemResponse;
import com.minegocio.mi_negocio.dtos.order.OrderResponse;
import com.minegocio.mi_negocio.entity.CustomerOrder;
import com.minegocio.mi_negocio.entity.OrderItem;
import com.minegocio.mi_negocio.entity.Product;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.entity.User;

public final class OrderMapper {

    private OrderMapper() {}

    public static OrderItemResponse toItemResponse(OrderItem item) {
        Product p = item.getProduct();
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new OrderItemResponse(
                item.getId(),
                p.getId(),
                p.getName(),
                p.getImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal
        );
    }

    public static OrderResponse toResponse(CustomerOrder order) {
        Store store = order.getStore();
        User creator = order.getCreatedBy();
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                store.getId(),
                store.getName(),
                creator.getId(),
                creator.getFullName(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getDeliveryAddress(),
                order.getNotes(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}