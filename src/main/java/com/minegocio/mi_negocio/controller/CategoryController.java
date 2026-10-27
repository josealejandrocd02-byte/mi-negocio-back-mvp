package com.minegocio.mi_negocio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.catalog.CategoryCreateRequest;
import com.minegocio.mi_negocio.dtos.catalog.CategoryResponse;
import com.minegocio.mi_negocio.dtos.catalog.CategoryUpdateRequest;
import com.minegocio.mi_negocio.dtos.user.ActiveRequest;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.CategoryService;

import jakarta.validation.Valid;

/**
 * Categorias de una sucursal (RF-19, RF-22).
 * Cualquier usuario autenticado llega aqui; el servicio exige STORE_ADMIN activo
 * para modificar y miembro activo para consultar. El id de quien llama sale del JWT.
 */
@RestController
@RequestMapping("/api/stores/{storeId}/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // Admin: todas. Empleado: solo activas.
    @GetMapping
    public List<CategoryResponse> list(@PathVariable UUID storeId,
                                       @AuthenticationPrincipal JwtPrincipal principal) {
        return service.list(storeId, principal.getUserId());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@PathVariable UUID storeId,
                                                   @AuthenticationPrincipal JwtPrincipal principal,
                                                   @Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(storeId, principal.getUserId(), request));
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse update(@PathVariable UUID storeId, @PathVariable UUID categoryId,
                                   @AuthenticationPrincipal JwtPrincipal principal,
                                   @Valid @RequestBody CategoryUpdateRequest request) {
        return service.update(storeId, principal.getUserId(), categoryId, request);
    }

    // { "active": false }
    @PatchMapping("/{categoryId}/active")
    public CategoryResponse setActive(@PathVariable UUID storeId, @PathVariable UUID categoryId,
                                      @AuthenticationPrincipal JwtPrincipal principal,
                                      @Valid @RequestBody ActiveRequest request) {
        return service.setActive(storeId, principal.getUserId(), categoryId, request.getActive());
    }
}
