package com.minegocio.mi_negocio.repository;

import java.util.List; 
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minegocio.mi_negocio.emus.OrderStatus;
import com.minegocio.mi_negocio.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {
 List<CustomerOrder> findByStore_IdOrderByCreatedAtDesc(UUID storeId);
 List<CustomerOrder> findByStore_IdAndStatusOrderByCreatedAtDesc(UUID storeId, OrderStatus status);
 Optional<CustomerOrder> findByIdAndStore_Id(UUID id, UUID storeId);
}