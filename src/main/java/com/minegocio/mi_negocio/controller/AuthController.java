package com.minegocio.mi_negocio.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.minegocio.mi_negocio.dtos.AuthResponse;
import com.minegocio.mi_negocio.dtos.LoginRequest;
import com.minegocio.mi_negocio.jwtconfig.CustomUserDetails;
import com.minegocio.mi_negocio.jwtconfig.JWTUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException e) {
            // credenciales incorrectas o usuario desactivado
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        String role = user.getPlatformRole().name();

        String token = jwtUtil.generateToken(user.getEmail(), role, user.getUserId().toString());

        return ResponseEntity.ok(
                new AuthResponse(token, user.getUserId(), user.getEmail(), user.getFullName(), role));
    }
}
