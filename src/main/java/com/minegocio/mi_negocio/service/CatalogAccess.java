package com.minegocio.mi_negocio.service;

import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.forbidden;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.minegocio.mi_negocio.emus.StoreRole;
import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.repository.StoreRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;

/**
 * Reglas comunes de acceso al catalogo (RF-36 / RF-37).
 * La identidad (callerId) sale siempre del token JWT.
 * - requireManager: STORE_ADMIN activo de la sucursal (escritura).
 * - requireMember : miembro activo (STORE_ADMIN o EMPLOYEE) de la sucursal (lectura).
 * En ambos casos: cuenta no bloqueada y sucursal ACTIVE.
 */
@Component
public class CatalogAccess {

    private final UserRepository userRepository;
    private final StoreUserRepository storeUserRepository;
    private final StoreRepository storeRepository;

    public CatalogAccess(UserRepository userRepository,
                         StoreUserRepository storeUserRepository,
                         StoreRepository storeRepository) {
        this.userRepository = userRepository;
        this.storeUserRepository = storeUserRepository;
        this.storeRepository = storeRepository;
    }

    public Store requireManager(UUID storeId, UUID callerId) {
        boolean allowed = userRepository.existsByIdAndActiveTrue(callerId)
                && storeUserRepository.existsByStore_IdAndUser_IdAndRoleAndActiveTrue(
                        storeId, callerId, StoreRole.STORE_ADMIN);
        if (!allowed) {
            throw forbidden("No tienes permisos de administrador en esta sucursal");
        }
        return requireActiveStore(storeId);
    }

    public Store requireMember(UUID storeId, UUID callerId) {
        boolean allowed = userRepository.existsByIdAndActiveTrue(callerId)
                && storeUserRepository.existsByStore_IdAndUser_IdAndActiveTrue(storeId, callerId);
        if (!allowed) {
            throw forbidden("No tienes acceso a esta sucursal");
        }
        return requireActiveStore(storeId);
    }

    // Tras requireMember: distingue STORE_ADMIN de EMPLOYEE (RF-22)
    public boolean isManager(UUID storeId, UUID callerId) {
        return storeUserRepository.existsByStore_IdAndUser_IdAndRoleAndActiveTrue(
                storeId, callerId, StoreRole.STORE_ADMIN);
    }

    private Store requireActiveStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> notFound("Sucursal no encontrada"));
        if (store.getStatus() != StoreStatus.ACTIVE) {
            throw conflict("La sucursal no esta activa");
        }
        return store;
    }
}
