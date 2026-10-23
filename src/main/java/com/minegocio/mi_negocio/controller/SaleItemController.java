package com.minegocio.mi_negocio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.sale.SaleItemResponse;
import com.minegocio.mi_negocio.dtos.saleitem.SaleItemCreateRequest;
import com.minegocio.mi_negocio.dtos.saleitem.SaleItemUpdateRequest;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.SaleItemServi;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sales")
public class SaleItemController {

    private final SaleItemServi saleItemService;

    public SaleItemController(SaleItemServi saleItemService) {
        this.saleItemService = saleItemService;
    }

    // POST /api/sales/{saleId}/items -> Agregar un ítem a la venta
    @PostMapping("/{saleId}/items")
    public ResponseEntity<SaleItemResponse> addItemToSale(
            @PathVariable UUID saleId,
            @Valid @RequestBody SaleItemCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        SaleItemResponse response = saleItemService.addItemToSale(saleId, request, principal.getUserId(), principal.isSuperAdmin());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/sales/{saleId}/items -> Listar todos los ítems de una venta
    @GetMapping("/{saleId}/items")
    public List<SaleItemResponse> listItemsBySale(
            @PathVariable UUID saleId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return saleItemService.listItemsBySale(saleId, principal.getUserId(), principal.isSuperAdmin());
    }

    // GET /api/sales/items/{itemId} -> Obtener detalle de un ítem por su ID
    @GetMapping("/items/{itemId}")
    public SaleItemResponse getItemById(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return saleItemService.getItemById(itemId, principal.getUserId(), principal.isSuperAdmin());
    }

    // PUT /api/sales/items/{itemId} -> Modificar la cantidad de un ítem existente
    @PutMapping("/items/{itemId}")
    public SaleItemResponse updateSaleItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody SaleItemUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return saleItemService.updateSaleItem(itemId, request, principal.getUserId(), principal.isSuperAdmin());
    }

    // DELETE /api/sales/items/{itemId} -> Eliminar un ítem de la venta
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteSaleItem(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        saleItemService.deleteSaleItem(itemId, principal.getUserId(), principal.isSuperAdmin());
        return ResponseEntity.noContent().build();
    }
}