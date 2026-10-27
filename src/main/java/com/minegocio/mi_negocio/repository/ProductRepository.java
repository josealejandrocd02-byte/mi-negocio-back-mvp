package com.minegocio.mi_negocio.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

 List<Product> findByStore_IdAndActiveTrueOrderByNameAsc(UUID storeId);

 List<Product> findByStore_IdAndCategory_Id(UUID storeId, UUID categoryId);

 Optional<Product> findByIdAndStore_Id(UUID id, UUID storeId);

 List<Product> findByStore_IdAndIdIn(UUID storeId, Collection<UUID> ids);

 boolean existsByStore_IdAndNameIgnoreCase(UUID storeId, String name);

 boolean existsByStore_IdAndNameIgnoreCaseAndIdNot(UUID storeId, String name, UUID id);

 // ==========================================
 // AGREGAR ESTOS DOS MÉTODOS:
 // ==========================================

 @Query("""
           select p from Product p 
           left join fetch p.category 
           where p.store.id = :storeId 
           and (:categoryId is null or p.category.id = :categoryId) 
           and (:onlyActive = false or p.active = true)
           order by p.name asc
           """)
 List<Product> findForList(@Param("storeId") UUID storeId,
                           @Param("categoryId") UUID categoryId,
                           @Param("onlyActive") boolean onlyActive);

 @Query("""
           select p from Product p 
           left join fetch p.category 
           where p.id = :productId and p.store.id = :storeId
           """)
 Optional<Product> findDetail(@Param("productId") UUID productId,
                              @Param("storeId") UUID storeId);
}