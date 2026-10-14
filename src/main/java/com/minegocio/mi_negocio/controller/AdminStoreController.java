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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.store.StoreCreateRequest;
import com.minegocio.mi_negocio.dtos.store.StoreDetailResponse;
import com.minegocio.mi_negocio.dtos.store.StoreResponse;
import com.minegocio.mi_negocio.dtos.store.StoreStatusRequest;
import com.minegocio.mi_negocio.dtos.store.StoreUpdateRequest;
import com.minegocio.mi_negocio.dtos.store.StoreUserLinkRequest;
import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.service.admin.AdminStoreService;

import jakarta.validation.Valid;

/**
 * Solo SUPER_ADMIN: SecurityConfig exige ROLE_SUPER_ADMIN para /api/admin/**.
 */
@RestController
@RequestMapping("/api/admin/stores")
public class AdminStoreController {

    private final AdminStoreService service;

    public AdminStoreController(AdminStoreService service) {
        this.service = service;
    }

    // GET /api/admin/stores            -> todas
    // GET /api/admin/stores?status=ACTIVE
    @GetMapping
    public List<StoreResponse> list(@RequestParam(required = false) StoreStatus status) {
        return service.list(status);
    }

    // Sucursal + su equipo (dueno, admins y empleados)
    @GetMapping("/{id}")
    public StoreDetailResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    // Crea la sucursal y vincula al dueno como STORE_ADMIN
    @PostMapping
    public ResponseEntity<StoreDetailResponse> create(@Valid @RequestBody StoreCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public StoreDetailResponse update(@PathVariable UUID id, @Valid @RequestBody StoreUpdateRequest request) {
        return service.update(id, request);
    }

    // { "status": "SUSPENDED" }
    @PatchMapping("/{id}/status")
    public StoreDetailResponse setStatus(@PathVariable UUID id, @Valid @RequestBody StoreStatusRequest request) {
        return service.setStatus(id, request.getStatus());
    }

    // "Eliminar" = cerrar (no borra historial)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> close(@PathVariable UUID id) {
        service.close(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- dueno y administradores ----------

    // Cambiar el dueno: { "userId": "..." }
    @PutMapping("/{id}/owner")
    public StoreDetailResponse changeOwner(@PathVariable UUID id, @Valid @RequestBody StoreUserLinkRequest request) {
        return service.changeOwner(id, request.getUserId());
    }

    // Vincular otro STORE_ADMIN: { "userId": "..." }
    @PostMapping("/{id}/admins")
    public StoreDetailResponse addAdmin(@PathVariable UUID id, @Valid @RequestBody StoreUserLinkRequest request) {
        return service.addAdmin(id, request.getUserId());
    }

    // Desvincular a un admin o empleado de la sucursal
    @DeleteMapping("/{id}/members/{userId}")
    public StoreDetailResponse removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        return service.removeMember(id, userId);
    }
}
