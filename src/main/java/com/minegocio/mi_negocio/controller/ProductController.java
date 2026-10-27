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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.catalog.ProductCreateRequest;
import com.minegocio.mi_negocio.dtos.catalog.ProductResponse;
import com.minegocio.mi_negocio.dtos.catalog.ProductUpdateRequest;
import com.minegocio.mi_negocio.dtos.user.ActiveRequest;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.ProductService;

import jakarta.validation.Valid;

/**
 * Productos de una sucursal, sin control de stock (RF-19..RF-23).
 * El servicio exige STORE_ADMIN activo para modificar y miembro activo para
 * consultar (el empleado solo ve activos). El id de quien llama sale del JWT.
 * La foto del producto se gestiona en /api/stores/{storeId}/foto-config.
 */
@RestController
@RequestMapping("/api/stores/{storeId}/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // GET /api/stores/{storeId}/products?categoryId=&q=
    @GetMapping
    public List<ProductResponse> list(@PathVariable UUID storeId,
                                      @AuthenticationPrincipal JwtPrincipal principal,
                                      @RequestParam(required = false) UUID categoryId,
                                      @RequestParam(required = false) String q) {
        return service.list(storeId, principal.getUserId(), categoryId, q);
    }

    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable UUID storeId, @PathVariable UUID productId,
                               @AuthenticationPrincipal JwtPrincipal principal) {
        return service.get(storeId, principal.getUserId(), productId);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@PathVariable UUID storeId,
                                                  @AuthenticationPrincipal JwtPrincipal principal,
                                                  @Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(storeId, principal.getUserId(), request));
    }

    @PutMapping("/{productId}")
    public ProductResponse update(@PathVariable UUID storeId, @PathVariable UUID productId,
                                  @AuthenticationPrincipal JwtPrincipal principal,
                                  @Valid @RequestBody ProductUpdateRequest request) {
        return service.update(storeId, principal.getUserId(), productId, request);
    }

    // { "active": false } -> ocultar producto agotado (RF-21)
    @PatchMapping("/{productId}/active")
    public ProductResponse setActive(@PathVariable UUID storeId, @PathVariable UUID productId,
                                     @AuthenticationPrincipal JwtPrincipal principal,
                                     @Valid @RequestBody ActiveRequest request) {
        return service.setActive(storeId, principal.getUserId(), productId, request.getActive());
    }
}
