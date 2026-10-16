package com.minegocio.mi_negocio.service.admin;

import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minegocio.mi_negocio.dtos.store.StoreCreateRequest;
import com.minegocio.mi_negocio.dtos.store.StoreDetailResponse;
import com.minegocio.mi_negocio.dtos.store.StoreResponse;
import com.minegocio.mi_negocio.dtos.store.StoreUpdateRequest;
import com.minegocio.mi_negocio.emus.PlatformRole;
import com.minegocio.mi_negocio.emus.StoreRole;
import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.entity.StoreUser;
import com.minegocio.mi_negocio.entity.User;
import com.minegocio.mi_negocio.repository.StoreRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;
import com.minegocio.mi_negocio.util.StoreMapper;

/**
 * Gestion de sucursales exclusiva del SUPER_ADMIN.
 * - Crea sucursales y decide quien es el dueno (puede tener 1 o mas).
 * - Vincula STORE_ADMIN adicionales.
 * - "Eliminar" = cerrar (status CLOSED): se conserva el historial de ventas y pedidos.
 */
@Service
public class AdminStoreService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final UserRepository userRepository;

    public AdminStoreService(StoreRepository storeRepository,
                             StoreUserRepository storeUserRepository,
                             UserRepository userRepository) {
        this.storeRepository = storeRepository;
        this.storeUserRepository = storeUserRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    @Transactional(readOnly = true)
    public List<StoreResponse> list(StoreStatus status) {
        List<Store> stores = (status == null)
                ? storeRepository.findAllWithOwner()
                : storeRepository.findAllWithOwnerByStatus(status);
        return stores.stream().map(StoreMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StoreDetailResponse get(UUID id) {
        return toDetail(findStore(id));
    }

    // =====================================================
    // CRUD
    // =====================================================

    @Transactional
    public StoreDetailResponse create(StoreCreateRequest req) {
        User owner = findEligibleUser(req.getOwnerUserId());

        String name = req.getName().trim();
        if (storeRepository.existsByOwnerUser_IdAndNameIgnoreCase(owner.getId(), name)) {
            throw conflict("Ese dueno ya tiene una sucursal con ese nombre");
        }

        Store store = new Store();
        store.setOwnerUser(owner);
        store.setName(name);
        store.setAddress(blankToNull(req.getAddress()));
        store.setPhone(blankToNull(req.getPhone()));
        store.setStatus(StoreStatus.ACTIVE);
        storeRepository.save(store);

        // El dueno siempre es STORE_ADMIN de su sucursal
        upsertMembership(store, owner, StoreRole.STORE_ADMIN);

        return toDetail(store);
    }

    @Transactional
    public StoreDetailResponse update(UUID id, StoreUpdateRequest req) {
        Store store = findOpenStore(id);

        String name = req.getName().trim();
        if (storeRepository.existsByOwnerUser_IdAndNameIgnoreCaseAndIdNot(
                store.getOwnerUser().getId(), name, id)) {
            throw conflict("El dueno ya tiene otra sucursal con ese nombre");
        }

        store.setName(name);
        store.setAddress(blankToNull(req.getAddress()));
        store.setPhone(blankToNull(req.getPhone()));
        return toDetail(store);
    }

    // ACTIVE, SUSPENDED o CLOSED (tambien sirve para reabrir una sucursal cerrada)
    @Transactional
    public StoreDetailResponse setStatus(UUID id, StoreStatus status) {
        Store store = findStore(id);
        store.setStatus(status);
        return toDetail(store);
    }

    // "Eliminar": cierra la sucursal sin borrar nada
    @Transactional
    public void close(UUID id) {
        Store store = findStore(id);
        store.setStatus(StoreStatus.CLOSED);
    }

    // =====================================================
    // DUENO Y ADMINISTRADORES
    // =====================================================

    // Cambia el dueno. El anterior sigue como STORE_ADMIN hasta que se le desvincule.
    @Transactional
    public StoreDetailResponse changeOwner(UUID id, UUID newOwnerId) {
        Store store = findOpenStore(id);
        User newOwner = findEligibleUser(newOwnerId);

        if (store.getOwnerUser().getId().equals(newOwnerId)) {
            throw conflict("Ese usuario ya es el dueno de la sucursal");
        }
        if (storeRepository.existsByOwnerUser_IdAndNameIgnoreCaseAndIdNot(newOwnerId, store.getName(), id)) {
            throw conflict("El nuevo dueno ya tiene una sucursal con ese nombre");
        }

        store.setOwnerUser(newOwner);
        upsertMembership(store, newOwner, StoreRole.STORE_ADMIN);
        return toDetail(store);
    }

    // Vincula un usuario como STORE_ADMIN (si era EMPLOYEE, lo promueve)
    @Transactional
    public StoreDetailResponse addAdmin(UUID id, UUID userId) {
        Store store = findOpenStore(id);
        User user = findEligibleUser(userId);
        upsertMembership(store, user, StoreRole.STORE_ADMIN);
        return toDetail(store);
    }

    // Desvincula a un usuario (admin o empleado) de la sucursal
    @Transactional
    public StoreDetailResponse removeMember(UUID id, UUID userId) {
        Store store = findStore(id);

        StoreUser su = storeUserRepository.findByStore_IdAndUser_Id(id, userId)
                .orElseThrow(() -> notFound("El usuario no pertenece a esta sucursal"));

        if (store.getOwnerUser().getId().equals(userId)) {
            throw conflict("No se puede desvincular al dueno; cambia primero el dueno de la sucursal");
        }

        storeUserRepository.delete(su);
        return toDetail(store);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Store findStore(UUID id) {
        return storeRepository.findByIdWithOwner(id)
                .orElseThrow(() -> notFound("Sucursal no encontrada"));
    }

    private Store findOpenStore(UUID id) {
        Store store = findStore(id);
        if (store.getStatus() == StoreStatus.CLOSED) {
            throw conflict("La sucursal esta cerrada; reabrela primero");
        }
        return store;
    }

    // Usuario valido para ser dueno o admin: existe, esta activo y no es SUPER_ADMIN
    private User findEligibleUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> notFound("Usuario no encontrado"));
        if (user.getPlatformRole() == PlatformRole.SUPER_ADMIN) {
            throw conflict("Un SUPER_ADMIN no puede ser dueno ni administrador de sucursal");
        }
        if (!user.isActive()) {
            throw conflict("El usuario esta bloqueado");
        }
        return user;
    }

    private void upsertMembership(Store store, User user, StoreRole role) {
        StoreUser su = storeUserRepository.findByStore_IdAndUser_Id(store.getId(), user.getId())
                .orElseGet(() -> {
                    StoreUser created = new StoreUser();
                    created.setStore(store);
                    created.setUser(user);
                    return created;
                });
        su.setRole(role);
        su.setActive(true);
        storeUserRepository.save(su);
    }

    private StoreDetailResponse toDetail(Store store) {
        return new StoreDetailResponse(
                StoreMapper.toResponse(store),
                storeUserRepository.findWithUserByStoreId(store.getId()).stream()
                        .map(StoreMapper::toMember)
                        .toList());
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
