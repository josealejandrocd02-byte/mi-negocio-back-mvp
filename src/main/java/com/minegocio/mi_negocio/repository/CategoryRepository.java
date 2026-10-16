package com.minegocio.mi_negocio.repository;


import java.util.List; 
import java.util.Optional;
import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;

import com.minegocio.mi_negocio.entity.Category;


public interface CategoryRepository extends JpaRepository<Category, UUID> {
 List<Category> findByStore_IdOrderByNameAsc(UUID storeId);
 Optional<Category> findByIdAndStore_Id(UUID id, UUID storeId);
 boolean existsByStore_IdAndNameIgnoreCase(UUID storeId, String name);
}