package com.minegocio.mi_negocio.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minegocio.mi_negocio.dtos.store.PhotoResponse;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.StoreFotoConfigService;

/**
 * Logo de la sucursal y fotos de sus productos.
 * Todas las subidas son multipart/form-data con el campo "file".
 * SecurityConfig exige token; el servicio comprueba:
 * - logo: SUPER_ADMIN o STORE_ADMIN de {storeId};
 * - foto de producto: solo STORE_ADMIN de {storeId} (el catalogo no lo gestiona SUPER_ADMIN).
 */
@RestController
@RequestMapping("/api/stores/{storeId}/foto-config")
public class StoreFotoConfigController {

    private final StoreFotoConfigService service;

    public StoreFotoConfigController(StoreFotoConfigService service) {
        this.service = service;
    }

    // ---------- logo ----------

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PhotoResponse uploadLogo(@PathVariable UUID storeId,
                                    @AuthenticationPrincipal JwtPrincipal principal,
                                    @RequestParam("file") MultipartFile file) {
        return service.uploadLogo(storeId, principal.getUserId(), principal.isSuperAdmin(), file);
    }

    @DeleteMapping("/logo")
    public ResponseEntity<Void> deleteLogo(@PathVariable UUID storeId,
                                           @AuthenticationPrincipal JwtPrincipal principal) {
        service.deleteLogo(storeId, principal.getUserId(), principal.isSuperAdmin());
        return ResponseEntity.noContent().build();
    }

    // ---------- foto de producto ----------

    @PostMapping(value = "/products/{productId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PhotoResponse uploadProductPhoto(@PathVariable UUID storeId, @PathVariable UUID productId,
                                            @AuthenticationPrincipal JwtPrincipal principal,
                                            @RequestParam("file") MultipartFile file) {
        return service.uploadProductPhoto(storeId, productId, principal.getUserId(), file);
    }

    @DeleteMapping("/products/{productId}/photo")
    public ResponseEntity<Void> deleteProductPhoto(@PathVariable UUID storeId, @PathVariable UUID productId,
                                                   @AuthenticationPrincipal JwtPrincipal principal) {
        service.deleteProductPhoto(storeId, productId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
