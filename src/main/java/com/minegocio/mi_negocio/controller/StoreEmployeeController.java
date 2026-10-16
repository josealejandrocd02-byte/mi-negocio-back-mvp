package com.minegocio.mi_negocio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.store.EmployeeCreateRequest;
import com.minegocio.mi_negocio.dtos.store.StoreMemberResponse;
import com.minegocio.mi_negocio.dtos.user.ActiveRequest;
import com.minegocio.mi_negocio.dtos.user.PasswordResetRequest;
import com.minegocio.mi_negocio.dtos.user.UserUpdateRequest;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.StoreEmployeeService;

import jakarta.validation.Valid;

/**
 * Para el STORE_ADMIN de una sucursal. Cualquier usuario autenticado llega aqui;
 * el servicio comprueba que sea STORE_ADMIN activo de {storeId}.
 */
@RestController
@RequestMapping("/api/stores/{storeId}/employees")
public class StoreEmployeeController {

    private final StoreEmployeeService service;

    public StoreEmployeeController(StoreEmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<StoreMemberResponse> list(@PathVariable UUID storeId,
                                          @AuthenticationPrincipal JwtPrincipal principal) {
        return service.list(storeId, principal.getUserId());
    }

    // Crea la cuenta del empleado y lo vincula a esta sucursal con rol EMPLOYEE
    @PostMapping
    public ResponseEntity<StoreMemberResponse> create(@PathVariable UUID storeId,
                                                      @AuthenticationPrincipal JwtPrincipal principal,
                                                      @Valid @RequestBody EmployeeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(storeId, principal.getUserId(), request));
    }

    @PutMapping("/{userId}")
    public StoreMemberResponse update(@PathVariable UUID storeId, @PathVariable UUID userId,
                                      @AuthenticationPrincipal JwtPrincipal principal,
                                      @Valid @RequestBody UserUpdateRequest request) {
        return service.update(storeId, principal.getUserId(), userId, request);
    }

    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID storeId, @PathVariable UUID userId,
                                              @AuthenticationPrincipal JwtPrincipal principal,
                                              @Valid @RequestBody PasswordResetRequest request) {
        service.resetPassword(storeId, principal.getUserId(), userId, request);
        return ResponseEntity.noContent().build();
    }

    // Restringir / devolver acceso a ESTA sucursal: { "active": false }
    @PatchMapping("/{userId}/active")
    public StoreMemberResponse setActive(@PathVariable UUID storeId, @PathVariable UUID userId,
                                         @AuthenticationPrincipal JwtPrincipal principal,
                                         @Valid @RequestBody ActiveRequest request) {
        return service.setActive(storeId, principal.getUserId(), userId, request.getActive());
    }

    // Desvincular de la sucursal
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@PathVariable UUID storeId, @PathVariable UUID userId,
                                       @AuthenticationPrincipal JwtPrincipal principal) {
        service.remove(storeId, principal.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }
}
