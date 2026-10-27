package com.minegocio.mi_negocio.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.entity.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
 List<SaleItem> findBySale_Id(UUID saleId);

 @Query("select si from SaleItem si join fetch si.product join fetch si.sale s join fetch s.store where si.id = :id")
 Optional<SaleItem> findByIdWithDetails(@Param("id") UUID id);
}