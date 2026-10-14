package com.minegocio.mi_negocio.service.admin;

import java.util.HashMap; 
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.minegocio.mi_negocio.dtos.user.PasswordResetRequest;
import com.minegocio.mi_negocio.dtos.user.StoreAssignmentRequest;
import com.minegocio.mi_negocio.dtos.user.UserCreateRequest;
import com.minegocio.mi_negocio.dtos.user.UserResponse;
import com.minegocio.mi_negocio.dtos.user.UserStoreResponse;
import com.minegocio.mi_negocio.dtos.user.UserUpdateRequest;
import com.minegocio.mi_negocio.emus.PlatformRole;
import com.minegocio.mi_negocio.emus.StoreRole;
import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.entity.StoreUser;
import com.minegocio.mi_negocio.entity.User;
import com.minegocio.mi_negocio.repository.StoreRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;

/**
 * CRUD de usuarios exclusivo del SUPER_ADMIN.
 * Todo usuario creado aqui es platformRole = USER; el SUPER_ADMIN solo se crea por SQL.
 * Ser STORE_ADMIN o EMPLOYEE depende de la sucursal (tabla store_users).
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository,
                            StoreRepository storeRepository,
                            StoreUserRepository storeUserRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.storeUserRepository = storeUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (users.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = users.stream().map(User::getId).toList();
        Map<UUID, List<StoreUser>> byUser = storeUserRepository.findWithStoreByUserIds(ids).stream()
                .collect(Collectors.groupingBy(su -> su.getUser().getId()));

        return users.stream()
                .map(u -> toResponse(u, byUser.getOrDefault(u.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> notFound("Usuario no encontrado"));
        return toResponse(user);
    }

    // =====================================================
    // CRUD
    // =====================================================

    @Transactional
    public UserResponse create(UserCreateRequest req) {
        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw conflict("Ya existe un usuario con ese email");
        }

        // Validar TODAS las sucursales antes de guardar nada
        List<StoreAssignmentRequest> assignments = req.getStores() == null ? List.of() : req.getStores();
        Set<UUID> seen = new HashSet<>();
        Map<UUID, Store> stores = new HashMap<>();
        for (StoreAssignmentRequest a : assignments) {
            if (!seen.add(a.getStoreId())) {
                throw badRequest("Sucursal repetida en la lista");
            }
            stores.put(a.getStoreId(), findAssignableStore(a.getStoreId()));
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setPlatformRole(PlatformRole.USER);
        user.setActive(true);
        userRepository.save(user);

        for (StoreAssignmentRequest a : assignments) {
            StoreUser su = new StoreUser();
            su.setStore(stores.get(a.getStoreId()));
            su.setUser(user);
            su.setRole(a.getRole());
            su.setActive(true);
            storeUserRepository.save(su);
        }

        return toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest req) {
        User user = findManageable(id);

        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw conflict("Ya existe otro usuario con ese email");
        }

        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        return toResponse(user);
    }

    @Transactional
    public void resetPassword(UUID id, PasswordResetRequest req) {
        User user = findManageable(id);
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
    }

    // Bloquea o desbloquea el acceso del usuario a TODO el sistema
    @Transactional
    public UserResponse setActive(UUID id, boolean active) {
        User user = findManageable(id);
        user.setActive(active);
        return toResponse(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = findManageable(id);

        if (storeRepository.existsByOwnerUser_Id(id)) {
            throw conflict("El usuario es dueno de una sucursal; reasigna la sucursal antes de eliminarlo");
        }

        try {
            storeUserRepository.deleteByUser_Id(id);
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // Tiene ventas/pedidos: se revierte todo (incluidas las desvinculaciones)
            throw conflict("El usuario tiene ventas o pedidos registrados. Desactivalo en lugar de eliminarlo");
        }
    }

    // =====================================================
    // SUCURSALES DEL USUARIO
    // =====================================================

    // Liga el usuario a una sucursal. Si ya pertenece, actualiza su rol y lo reactiva.
    @Transactional
    public UserResponse assignStore(UUID userId, StoreAssignmentRequest req) {
        User user = findManageable(userId);
        Store store = findAssignableStore(req.getStoreId());

        if (isOwner(store, userId) && req.getRole() != StoreRole.STORE_ADMIN) {
            throw conflict("El dueno de la sucursal debe ser STORE_ADMIN");
        }

        StoreUser su = storeUserRepository.findByStore_IdAndUser_Id(store.getId(), userId)
                .orElseGet(() -> {
                    StoreUser created = new StoreUser();
                    created.setStore(store);
                    created.setUser(user);
                    return created;
                });

        su.setRole(req.getRole());
        su.setActive(true);
        storeUserRepository.save(su);

        return toResponse(user);
    }

    // Desvincula al usuario de una sucursal
    @Transactional
    public UserResponse unassignStore(UUID userId, UUID storeId) {
        User user = findManageable(userId);
        StoreUser su = findMembership(storeId, userId);

        if (isOwner(su.getStore(), userId)) {
            throw conflict("No se puede desvincular al dueno de la sucursal");
        }

        storeUserRepository.delete(su);
        return toResponse(user);
    }

    // Restringe o devuelve el acceso del usuario a UNA sucursal (sin desvincularlo)
    @Transactional
    public UserResponse setStoreAccess(UUID userId, UUID storeId, boolean active) {
        User user = findManageable(userId);
        StoreUser su = findMembership(storeId, userId);

        if (!active && isOwner(su.getStore(), userId)) {
            throw conflict("No se puede restringir al dueno de su propia sucursal");
        }

        su.setActive(active);
        return toResponse(user);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    // Busca el usuario y bloquea cualquier modificacion sobre un SUPER_ADMIN
    private User findManageable(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> notFound("Usuario no encontrado"));
        if (user.getPlatformRole() == PlatformRole.SUPER_ADMIN) {
            throw forbidden("No se puede modificar a un SUPER_ADMIN");
        }
        return user;
    }

    private Store findAssignableStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> notFound("Sucursal no encontrada: " + storeId));
        if (store.getStatus() == StoreStatus.CLOSED) {
            throw conflict("La sucursal '" + store.getName() + "' esta cerrada");
        }
        return store;
    }

    private StoreUser findMembership(UUID storeId, UUID userId) {
        return storeUserRepository.findByStore_IdAndUser_Id(storeId, userId)
                .orElseThrow(() -> notFound("El usuario no pertenece a esa sucursal"));
    }

    private boolean isOwner(Store store, UUID userId) {
        return store.getOwnerUser().getId().equals(userId);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse toResponse(User user) {
        return toResponse(user, storeUserRepository.findWithStoreByUserIds(List.of(user.getId())));
    }

    private UserResponse toResponse(User user, List<StoreUser> memberships) {
        List<UserStoreResponse> stores = memberships.stream()
                .map(su -> new UserStoreResponse(
                        su.getStore().getId(),
                        su.getStore().getName(),
                        su.getRole(),
                        su.isActive()))
                .toList();

        return new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(),
                user.getPlatformRole(), user.isActive(), user.getCreatedAt(), stores);
    }

    private static ResponseStatusException notFound(String msg) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }

    private static ResponseStatusException conflict(String msg) {
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }

    private static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private static ResponseStatusException forbidden(String msg) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, msg);
    }
}
