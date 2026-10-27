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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.order.OrderItemRequest;
import com.minegocio.mi_negocio.dtos.order.OrderItemResponse;
import com.minegocio.mi_negocio.dtos.order.OrderRequest;
import com.minegocio.mi_negocio.dtos.order.OrderResponse;
import com.minegocio.mi_negocio.dtos.order.OrderStatusRequest;
import com.minegocio.mi_negocio.emus.OrderStatus;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.CustomerOrderService;

import jakarta.validation.Valid;

/**
 * Endpoints de pedidos (orders) e items de pedido (order_items) de una sucursal.
 * Pueden usarlos miembros activos de {storeId} (STORE_ADMIN o EMPLOYEE) o el SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/stores/{storeId}/orders")
public class CustomerOrderController {

    private final CustomerOrderService service;

    public CustomerOrderController(CustomerOrderService service) {
        this.service = service;
    }

    // ---------- PEDIDOS (orders) ----------

    // GET /api/stores/{storeId}/orders
    // GET /api/stores/{storeId}/orders?status=PENDING
    @GetMapping
    public List<OrderResponse> list(@PathVariable UUID storeId,
                                    @RequestParam(required = false) OrderStatus status,
                                    @AuthenticationPrincipal JwtPrincipal principal) {
        return service.list(storeId, principal.getUserId(), principal.isSuperAdmin(), status);
    }

    // GET /api/stores/{storeId}/orders/{orderId}
    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable UUID storeId,
                             @PathVariable UUID orderId,
                             @AuthenticationPrincipal JwtPrincipal principal) {
        return service.get(storeId, orderId, principal.getUserId(), principal.isSuperAdmin());
    }

    // POST /api/stores/{storeId}/orders
    @PostMapping
    public ResponseEntity<OrderResponse> create(@PathVariable UUID storeId,
                                                @AuthenticationPrincipal JwtPrincipal principal,
                                                @Valid @RequestBody OrderRequest request) {
        OrderResponse created = service.create(
                storeId, principal.getUserId(), principal.isSuperAdmin(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/stores/{storeId}/orders/{orderId}
    @PutMapping("/{orderId}")
    public OrderResponse update(@PathVariable UUID storeId,
                                @PathVariable UUID orderId,
                                @AuthenticationPrincipal JwtPrincipal principal,
                                @Valid @RequestBody OrderRequest request) {
        return service.update(storeId, orderId, principal.getUserId(), principal.isSuperAdmin(), request);
    }

    // PATCH /api/stores/{storeId}/orders/{orderId}/status
    // { "status": "PREPARING" | "READY" | "DELIVERED" | "CANCELLED" }
    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable UUID storeId,
                                      @PathVariable UUID orderId,
                                      @AuthenticationPrincipal JwtPrincipal principal,
                                      @Valid @RequestBody OrderStatusRequest request) {
        return service.updateStatus(
                storeId, orderId, principal.getUserId(), principal.isSuperAdmin(), request.getStatus());
    }
    
    // DELETE /api/stores/{storeId}/orders/{orderId}
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> delete(@PathVariable UUID storeId,
                                       @PathVariable UUID orderId,
                                       @AuthenticationPrincipal JwtPrincipal principal) {
        service.delete(storeId, orderId, principal.getUserId(), principal.isSuperAdmin());
        return ResponseEntity.noContent().build();
    }

    // ---------- ITEMS DEL PEDIDO (order_items) ----------

    // GET /api/stores/{storeId}/orders/{orderId}/items
    @GetMapping("/{orderId}/items")
    public List<OrderItemResponse> listItems(@PathVariable UUID storeId,
                                             @PathVariable UUID orderId,
                                             @AuthenticationPrincipal JwtPrincipal principal) {
        return service.listItems(storeId, orderId, principal.getUserId(), principal.isSuperAdmin());
    }

    // POST /api/stores/{storeId}/orders/{orderId}/items
    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addItem(@PathVariable UUID storeId,
                                                 @PathVariable UUID orderId,
                                                 @AuthenticationPrincipal JwtPrincipal principal,
                                                 @Valid @RequestBody OrderItemRequest request) {
        OrderResponse updated = service.addItem(
                storeId, orderId, principal.getUserId(), principal.isSuperAdmin(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    // PUT /api/stores/{storeId}/orders/{orderId}/items/{itemId}
    @PutMapping("/{orderId}/items/{itemId}")
    public OrderResponse updateItem(@PathVariable UUID storeId,
                                    @PathVariable UUID orderId,
                                    @PathVariable UUID itemId,
                                    @AuthenticationPrincipal JwtPrincipal principal,
                                    @Valid @RequestBody OrderItemRequest request) {
        return service.updateItem(
                storeId, orderId, itemId, principal.getUserId(), principal.isSuperAdmin(), request);
    }

    // DELETE /api/stores/{storeId}/orders/{orderId}/items/{itemId}
    @DeleteMapping("/{orderId}/items/{itemId}")
    public OrderResponse removeItem(@PathVariable UUID storeId,
                                    @PathVariable UUID orderId,
                                    @PathVariable UUID itemId,
                                    @AuthenticationPrincipal JwtPrincipal principal) {
        return service.removeItem(storeId, orderId, itemId, principal.getUserId(), principal.isSuperAdmin());
    }
}