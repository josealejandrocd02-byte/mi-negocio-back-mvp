package com.minegocio.mi_negocio.repository;

import java.time.Instant; 
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
 List<Sale> findByStore_IdOrderByCreatedAtDesc(UUID storeId);
 List<Sale> findByStore_IdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID storeId, Instant from, Instant to);
 Optional<Sale> findByIdAndStore_Id(UUID id, UUID storeId);

 // Reporte consolidado de un dueño con varias sucursales
 @Query("""
        select s.store.name as storeName, count(s) as salesCount, sum(s.total) as total
        from Sale s
        where s.store.ownerUser.id = :ownerId and s.createdAt >= :since
        group by s.store.name
        """)
 List<StoreSalesSummary> summaryByOwner(@Param("ownerId") UUID ownerId, @Param("since") Instant since);
}