package com.minegocio.mi_negocio.repository;

import java.util.List; 
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minegocio.mi_negocio.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
 List<OrderItem> findByOrder_Id(UUID orderId);
}