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
import com.minegocio.mi_negocio.entity.Product;
import com.minegocio.mi_negocio.entity.Store;
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
@RequestMapping("/api/emplooy")
public class EmplooyController {

    private final ProductService service;

    public EmplooyController(ProductService service) {
        this.service = service;
    }


    
    @GetMapping("/list-products")
    public List<ProductResponse> listEmployee(
                                      @AuthenticationPrincipal JwtPrincipal principal,
                                      @RequestParam(required = false) UUID categoryId,
                                      @RequestParam(required = false) String q) {
    	
        return service.listEmployee(principal.getUserId(), categoryId, q);
    }


}
