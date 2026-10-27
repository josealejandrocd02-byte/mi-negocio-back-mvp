package com.minegocio.mi_negocio.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.emus.OrderStatus;
import com.minegocio.mi_negocio.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {

    List<CustomerOrder> findByStore_IdOrderByCreatedAtDesc(UUID storeId);

    List<CustomerOrder> findByStore_IdAndStatusOrderByCreatedAtDesc(UUID storeId, OrderStatus status);

    Optional<CustomerOrder> findByIdAndStore_Id(UUID id, UUID storeId);
    
    //-------------------IRAKO
    // Consultas con relaciones cargadas (evita N+1 y LazyInitializationException)
    @Query("""
            select distinct o from CustomerOrder o
            join fetch o.store s
            join fetch o.createdBy u
            left join fetch o.items i
            left join fetch i.product p
            where s.id = :storeId
            order by o.createdAt desc
            """)
    List<CustomerOrder> findAllWithDetailsByStoreId(@Param("storeId") UUID storeId);

    @Query("""
            select distinct o from CustomerOrder o
            join fetch o.store s
            join fetch o.createdBy u
            left join fetch o.items i
            left join fetch i.product p
            where s.id = :storeId and o.status = :status
            order by o.createdAt desc
            """)
    List<CustomerOrder> findAllWithDetailsByStoreIdAndStatus(
            @Param("storeId") UUID storeId,
            @Param("status") OrderStatus status);

    @Query("""
            select o from CustomerOrder o
            join fetch o.store s
            join fetch o.createdBy u
            left join fetch o.items i
            left join fetch i.product p
            where o.id = :id and s.id = :storeId
            """)
    Optional<CustomerOrder> findByIdAndStoreIdWithDetails(
            @Param("id") UUID id,
            @Param("storeId") UUID storeId);
}