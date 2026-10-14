package com.minegocio.mi_negocio.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minegocio.mi_negocio.emus.StoreRole;
import com.minegocio.mi_negocio.entity.StoreUser;

public interface StoreUserRepository extends JpaRepository<StoreUser, UUID> {
	// Sucursales a las que accede un usuario
	List<StoreUser> findByUser_IdAndActiveTrue(UUID userId);

	// Equipo de una sucursal
	List<StoreUser> findByStore_IdAndActiveTrue(UUID storeId);

	Optional<StoreUser> findByStore_IdAndUser_Id(UUID storeId, UUID userId);

	// Comprobación de autorización: ¿es admin activo de esta sucursal?
	boolean existsByStore_IdAndUser_IdAndRoleAndActiveTrue(UUID storeId, UUID userId, StoreRole role);

	// ¿pertenece (con cualquier rol) a esta sucursal?
	boolean existsByStore_IdAndUser_IdAndActiveTrue(UUID storeId, UUID userId);

	// Membresias de varios usuarios con su sucursal ya cargada (evita N+1)
	@Query("""
			select su from StoreUser su
			join fetch su.store s
			where su.user.id in :ids
			order by s.name
			""")
	List<StoreUser> findWithStoreByUserIds(@Param("ids") Collection<UUID> ids);

	// Al eliminar un usuario
	void deleteByUser_Id(UUID userId);

	// Equipo de una sucursal con el usuario cargado (admins y empleados)
	@Query("""
			select su from StoreUser su
			join fetch su.user u
			where su.store.id = :storeId
			order by u.fullName
			""")
	List<StoreUser> findWithUserByStoreId(@Param("storeId") UUID storeId);

	// En cuantas sucursales esta vinculado un usuario
	long countByUser_Id(UUID userId);

}
