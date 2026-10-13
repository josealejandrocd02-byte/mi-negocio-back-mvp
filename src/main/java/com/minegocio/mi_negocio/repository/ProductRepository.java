package com.minegocio.mi_negocio.repository;


import java.util.List; 
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minegocio.mi_negocio.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
 List<Product> findByStore_IdAndActiveTrueOrderByNameAsc(UUID storeId);
 List<Product> findByStore_IdAndCategory_Id(UUID storeId, UUID categoryId);
 Optional<Product> findByIdAndStore_Id(UUID id, UUID storeId);
}