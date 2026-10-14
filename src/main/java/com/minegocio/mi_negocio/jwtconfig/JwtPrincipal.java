package com.minegocio.mi_negocio.jwtconfig;

import java.util.UUID;

// Se obtiene en los controllers con: @AuthenticationPrincipal JwtPrincipal principal
public class JwtPrincipal {

    private final UUID userId;
    private final String email;
    private final String platformRole;

    public JwtPrincipal(UUID userId, String email, String platformRole) {
        this.userId = userId;
        this.email = email;
        this.platformRole = platformRole;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPlatformRole() {
        return platformRole;
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(platformRole);
    }
}
