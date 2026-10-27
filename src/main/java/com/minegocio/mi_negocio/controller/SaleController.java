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

import com.minegocio.mi_negocio.dtos.sale.SaleCreateRequest;
import com.minegocio.mi_negocio.dtos.sale.SaleResponse;
import com.minegocio.mi_negocio.dtos.sale.SaleStatusRequest;
import com.minegocio.mi_negocio.dtos.sale.SaleUpdateRequest;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.SaleServi;

import jakarta.validation.Valid;

/**
 * Para el dueno o los miembros de una sucursal (tambien SUPER_ADMIN).
 * Quien llama sale SIEMPRE del token JWT; el servicio comprueba el acceso a {storeId}.
 */
@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleServi saleService;

    // Inyección de dependencias por constructor
    public SaleController(SaleServi saleService) {
        this.saleService = saleService;
    }

    // POST /api/sales -> crear venta
    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleCreateRequest request,
                                                   @AuthenticationPrincipal JwtPrincipal principal) {
        SaleResponse sale = saleService.createSale(request, principal.getUserId(), principal.isSuperAdmin());
        return ResponseEntity.status(HttpStatus.CREATED).body(sale);
    }

    // GET /api/sales/store/{storeId} -> listado de ventas por tienda
    @GetMapping("/store/{storeId}")
    public List<SaleResponse> listSalesByStore(@PathVariable UUID storeId,
                                               @AuthenticationPrincipal JwtPrincipal principal) {
        return saleService.listSalesByStore(storeId, principal.getUserId(), principal.isSuperAdmin());
    }

    // GET /api/sales/{id} -> obtener detalle de una venta por id
    @GetMapping("/{id}")
    public SaleResponse getSaleById(@PathVariable UUID id,
                                   @AuthenticationPrincipal JwtPrincipal principal) {
        return saleService.getSaleById(id, principal.getUserId(), principal.isSuperAdmin());
    }

    // PUT /api/sales/{id} -> modificar items (recalcula el total)
    @PutMapping("/{id}")
    public SaleResponse updateSale(@PathVariable UUID id,
                                   @Valid @RequestBody SaleUpdateRequest request,
                                   @AuthenticationPrincipal JwtPrincipal principal) {
        return saleService.updateSale(id, request, principal.getUserId(), principal.isSuperAdmin());
    }

    // PATCH /api/sales/{id}/status -> modificar el estado
    @PatchMapping("/{id}/status")
    public SaleResponse changeStatus(@PathVariable UUID id,
                                     @Valid @RequestBody SaleStatusRequest request,
                                     @AuthenticationPrincipal JwtPrincipal principal) {
        return saleService.changeStatus(id, request, principal.getUserId(), principal.isSuperAdmin());
    }

    // PATCH /api/sales/{id}/cancel -> cancelacion logica (estado CANCELLED)
    @PatchMapping("/{id}/cancel")
    public SaleResponse cancelSale(@PathVariable UUID id,
                                   @AuthenticationPrincipal JwtPrincipal principal) {
        return saleService.cancelSale(id, principal.getUserId(), principal.isSuperAdmin());
    }

    // DELETE /api/sales/{id} -> eliminar (solo PENDING o CANCELLED)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable UUID id,
                                           @AuthenticationPrincipal JwtPrincipal principal) {
        saleService.deleteSale(id, principal.getUserId(), principal.isSuperAdmin());
        return ResponseEntity.noContent().build();
    }
}
