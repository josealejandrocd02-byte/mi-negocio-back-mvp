package com.minegocio.mi_negocio.controller;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.user.ActiveRequest;
import com.minegocio.mi_negocio.dtos.user.PasswordResetRequest;
import com.minegocio.mi_negocio.dtos.user.StoreAssignmentRequest;
import com.minegocio.mi_negocio.dtos.user.UserCreateRequest;
import com.minegocio.mi_negocio.dtos.user.UserResponse;
import com.minegocio.mi_negocio.dtos.user.UserUpdateRequest;
import com.minegocio.mi_negocio.service.admin.AdminUserService;

import jakarta.validation.Valid;

/**
 * Solo SUPER_ADMIN: SecurityConfig exige ROLE_SUPER_ADMIN para /api/admin/**.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID id,
                                              @Valid @RequestBody PasswordResetRequest request) {
        service.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    // Bloquear / desbloquear el acceso al sistema: { "active": false }
    @PatchMapping("/{id}/active")
    public UserResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return service.setActive(id, request.getActive());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- sucursales del usuario ----------

    // Ligar a una sucursal (o cambiar su rol): { "storeId": "...", "role": "STORE_ADMIN" }
    @PostMapping("/{id}/stores")
    public UserResponse assignStore(@PathVariable UUID id,
                                    @Valid @RequestBody StoreAssignmentRequest request) {
        return service.assignStore(id, request);
    }

    // Desvincular de una sucursal
    @DeleteMapping("/{id}/stores/{storeId}")
    public UserResponse unassignStore(@PathVariable UUID id, @PathVariable UUID storeId) {
        return service.unassignStore(id, storeId);
    }

    // Restringir / devolver acceso a UNA sucursal: { "active": false }
    @PatchMapping("/{id}/stores/{storeId}/active")
    public UserResponse setStoreAccess(@PathVariable UUID id, @PathVariable UUID storeId,
                                       @Valid @RequestBody ActiveRequest request) {
        return service.setStoreAccess(id, storeId, request.getActive());
    }
}
