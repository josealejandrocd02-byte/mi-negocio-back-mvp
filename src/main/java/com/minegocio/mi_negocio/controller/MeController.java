package com.minegocio.mi_negocio.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.minegocio.mi_negocio.dtos.user.UserResponse;
import com.minegocio.mi_negocio.dtos.user.UserStoreResponse;
import com.minegocio.mi_negocio.entity.StoreUser;
import com.minegocio.mi_negocio.entity.User;
import com.minegocio.mi_negocio.jwtconfig.JwtPrincipal;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;

/**
 * Datos del usuario con sesion iniciada. El id sale del token, no del front. joel guerrero
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserRepository userRepository;
    private final StoreUserRepository storeUserRepository;

    public MeController(UserRepository userRepository, StoreUserRepository storeUserRepository) {
        this.userRepository = userRepository;
        this.storeUserRepository = storeUserRepository;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal JwtPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        List<StoreUser> memberships = storeUserRepository.findWithStoreByUserIds(List.of(user.getId()));

        List<UserStoreResponse> stores = memberships.stream()
                .map(su -> new UserStoreResponse(
                        su.getStore().getId(),
                        su.getStore().getName(),
                        su.getRole(),
                        su.isActive()))
                .toList();

        return new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(),
                user.getPlatformRole(), user.isActive(), user.getCreatedAt(), stores);
    }
}
