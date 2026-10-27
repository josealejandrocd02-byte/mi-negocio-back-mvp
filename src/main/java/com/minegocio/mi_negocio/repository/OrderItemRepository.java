package com.minegocio.mi_negocio.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrder_Id(UUID orderId);
    
    //-------------------ISRAKO
    @Query("""
            select oi from OrderItem oi
            join fetch oi.product p
            where oi.order.id = :orderId and oi.storeId = :storeId
            """)
    List<OrderItem> findWithProductByOrderIdAndStoreId(
            @Param("orderId") UUID orderId,
            @Param("storeId") UUID storeId);

    @Query("""
            select oi from OrderItem oi
            join fetch oi.product p
            where oi.id = :itemId and oi.order.id = :orderId and oi.storeId = :storeId
            """)
    Optional<OrderItem> findByIdAndOrderIdAndStoreIdWithProduct(
            @Param("itemId") UUID itemId,
            @Param("orderId") UUID orderId,
            @Param("storeId") UUID storeId);
}