package com.minegocio.mi_negocio.repository;

import java.util.List; 
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.Store;

public interface StoreRepository extends JpaRepository<Store, UUID> {
 List<Store> findByOwnerUser_IdAndStatusNot(UUID ownerId, StoreStatus status);
 List<Store> findByStatusNot(StoreStatus status);   // listado del SUPER_ADMIN
 // Usado al eliminar un usuario: no se puede borrar a un dueno de sucursal
 boolean existsByOwnerUser_Id(UUID ownerId);
}