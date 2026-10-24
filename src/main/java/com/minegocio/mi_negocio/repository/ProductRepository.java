package com.minegocio.mi_negocio.repository;


import java.util.List; 
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {

 // Listado con la categoria cargada (evita N+1) y filtros/orden resueltos en BD.
 // activeOnly=true (empleado, RF-22): solo productos activos cuya categoria, si existe, tambien este activa.
 @Query("""
   select p from Product p
   left join fetch p.category c
   where p.store.id = :storeId
     and (:categoryId is null or c.id = :categoryId)
     and (:activeOnly = false
          or (p.active = true and (c is null or c.active = true)))
   order by p.name asc
   """)
 List<Product> findForList(@Param("storeId") UUID storeId,
                           @Param("categoryId") UUID categoryId,
                           @Param("activeOnly") boolean activeOnly);

 Optional<Product> findByIdAndStore_Id(UUID id, UUID storeId);

 boolean existsByStore_IdAndNameIgnoreCase(UUID storeId, String name);

 boolean existsByStore_IdAndNameIgnoreCaseAndIdNot(UUID storeId, String name, UUID id);

 // Detalle con la categoria cargada (evita LazyInitialization al mapear)
 @Query("select p from Product p left join fetch p.category where p.id = :id and p.store.id = :storeId")
 Optional<Product> findDetail(@Param("id") UUID id, @Param("storeId") UUID storeId);
}