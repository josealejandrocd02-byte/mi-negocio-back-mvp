package com.minegocio.mi_negocio.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minegocio.mi_negocio.dtos.user.UserResponse;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.service.admin.AdminUserService;

/**
 * Perfil del usuario autenticado y sus sucursales.
 * El frontend lo usa como GET /api/me para saber que locales puede administrar.
 * La identidad sale del JWT, nunca de un parametro de la peticion.
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final AdminUserService service;

    public MeController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal JwtPrincipal principal) {
        return service.get(principal.getUserId());
    }
}
