package com.minegocio.mi_negocio.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.Store;

public interface StoreRepository extends JpaRepository<Store, UUID> {

	// ---- listados con el dueno ya cargado (evita N+1) ----
	@Query("select s from Store s join fetch s.ownerUser order by s.createdAt desc")
	List<Store> findAllWithOwner();

	@Query("select s from Store s join fetch s.ownerUser where s.status = :status order by s.createdAt desc")
	List<Store> findAllWithOwnerByStatus(@Param("status") StoreStatus status);

	@Query("select s from Store s join fetch s.ownerUser where s.id = :id")
	Optional<Store> findByIdWithOwner(@Param("id") UUID id);

	// ---- por dueno ----

	List<Store> findByOwnerUser_IdAndStatusNot(UUID ownerId, StoreStatus status);

	List<Store> findByStatusNot(StoreStatus status); // listado del SUPER_ADMIN
	// Usado al eliminar un usuario: no se puede borrar a un dueno de sucursal

	boolean existsByOwnerUser_Id(UUID ownerId);

	// ---- nombre unico por dueno (sin distinguir mayusculas) ----
	boolean existsByOwnerUser_IdAndNameIgnoreCase(UUID ownerId, String name);
	
	@Query("""
		    SELECT su.store.id
		    FROM StoreUser su
		    WHERE su.user.id = :userId
		""")
		Optional<UUID> findStoreIdByUserId(@Param("userId") UUID userId);

	boolean existsByOwnerUser_IdAndNameIgnoreCaseAndIdNot(UUID ownerId, String name, UUID id);
}
