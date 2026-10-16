package com.minegocio.mi_negocio.repository;

import java.util.List; 
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minegocio.mi_negocio.entity.SaleItem;
 
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
 List<SaleItem> findBySale_Id(UUID saleId);
}