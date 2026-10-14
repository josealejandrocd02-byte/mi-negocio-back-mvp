package com.minegocio.mi_negocio.service;
import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.forbidden;
import static com.minegocio.mi_negocio.util.ApiErrors.normalizeEmail;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minegocio.mi_negocio.dtos.store.EmployeeCreateRequest;
import com.minegocio.mi_negocio.dtos.store.StoreMemberResponse;
import com.minegocio.mi_negocio.dtos.user.PasswordResetRequest;
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
import com.minegocio.mi_negocio.util.StoreMapper;

/**
 * Gestion de empleados por el STORE_ADMIN de una sucursal.
 * - Solo opera sobre usuarios con rol EMPLOYEE de ESA sucursal.
 * - El id de quien llama (callerId) sale SIEMPRE del token JWT, nunca del front.
 * - Los datos de la cuenta (nombre, email, clave) solo se tocan si el empleado
 *   pertenece unicamente a esta sucursal; si esta en varias, solo el SUPER_ADMIN.
 */
@Service
public class StoreEmployeeService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StoreEmployeeService(StoreRepository storeRepository,
                                StoreUserRepository storeUserRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.storeRepository = storeRepository;
        this.storeUserRepository = storeUserRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StoreMemberResponse> list(UUID storeId, UUID callerId) {
        requireStoreAdmin(storeId, callerId);
        return storeUserRepository.findWithUserByStoreId(storeId).stream()
                .filter(su -> su.getRole() == StoreRole.EMPLOYEE)
                .map(StoreMapper::toMember)
                .toList();
    }

    @Transactional
    public StoreMemberResponse create(UUID storeId, UUID callerId, EmployeeCreateRequest req) {
        Store store = requireStoreAdmin(storeId, callerId);

        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw conflict("Ya existe un usuario con ese email. "
                    + "Pide al administrador de la plataforma que lo vincule a tu sucursal");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setPlatformRole(PlatformRole.USER);
        user.setActive(true);
        userRepository.save(user);

        StoreUser su = new StoreUser();
        su.setStore(store);
        su.setUser(user);
        su.setRole(StoreRole.EMPLOYEE);
        su.setActive(true);
        storeUserRepository.save(su);

        return StoreMapper.toMember(su);
    }

    @Transactional
    public StoreMemberResponse update(UUID storeId, UUID callerId, UUID userId, UserUpdateRequest req) {
        requireStoreAdmin(storeId, callerId);
        StoreUser su = findEmployee(storeId, userId);
        requireExclusive(userId);

        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw conflict("Ya existe otro usuario con ese email");
        }

        User user = su.getUser();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        return StoreMapper.toMember(su);
    }

    @Transactional
    public void resetPassword(UUID storeId, UUID callerId, UUID userId, PasswordResetRequest req) {
        requireStoreAdmin(storeId, callerId);
        StoreUser su = findEmployee(storeId, userId);
        requireExclusive(userId);

        su.getUser().setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
    }

    // Restringe o devuelve el acceso del empleado a ESTA sucursal
    @Transactional
    public StoreMemberResponse setActive(UUID storeId, UUID callerId, UUID userId, boolean active) {
        requireStoreAdmin(storeId, callerId);
        StoreUser su = findEmployee(storeId, userId);
        su.setActive(active);
        return StoreMapper.toMember(su);
    }

    // Desvincula al empleado de la sucursal (su cuenta se conserva)
    @Transactional
    public void remove(UUID storeId, UUID callerId, UUID userId) {
        requireStoreAdmin(storeId, callerId);
        StoreUser su = findEmployee(storeId, userId);
        storeUserRepository.delete(su);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    // Quien llama debe ser STORE_ADMIN activo de la sucursal, con cuenta activa
    private Store requireStoreAdmin(UUID storeId, UUID callerId) {
        boolean allowed = userRepository.existsByIdAndActiveTrue(callerId)
                && storeUserRepository.existsByStore_IdAndUser_IdAndRoleAndActiveTrue(
                        storeId, callerId, StoreRole.STORE_ADMIN);
        if (!allowed) {
            throw forbidden("No tienes permisos de administrador en esta sucursal");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> notFound("Sucursal no encontrada"));
        if (store.getStatus() != StoreStatus.ACTIVE) {
            throw conflict("La sucursal no esta activa");
        }
        return store;
    }

    // Solo membresias con rol EMPLOYEE: un STORE_ADMIN no puede tocar a otros admins ni al dueno
    private StoreUser findEmployee(UUID storeId, UUID userId) {
        return storeUserRepository.findByStore_IdAndUser_Id(storeId, userId)
                .filter(su -> su.getRole() == StoreRole.EMPLOYEE)
                .orElseThrow(() -> notFound("Empleado no encontrado en esta sucursal"));
    }

    private void requireExclusive(UUID userId) {
        if (storeUserRepository.countByUser_Id(userId) > 1) {
            throw forbidden("El empleado trabaja en otras sucursales; "
                    + "solo el administrador de la plataforma puede modificar su cuenta");
        }
    }
}
