package com.minegocio.mi_negocio.dtos.store;

import java.time.Instant;
import java.util.UUID;

import com.minegocio.mi_negocio.emus.StoreRole;

// Un usuario dentro de una sucursal (STORE_ADMIN o EMPLOYEE)
public class StoreMemberResponse {

    private final UUID userId;
    private final String fullName;
    private final String email;
    private final StoreRole role;
    private final boolean userActive;   // cuenta bloqueada o no en todo el sistema
    private final boolean active;       // acceso a ESTA sucursal
    private final Instant joinedAt;

    public StoreMemberResponse(UUID userId, String fullName, String email, StoreRole role,
                               boolean userActive, boolean active, Instant joinedAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.userActive = userActive;
        this.active = active;
        this.joinedAt = joinedAt;
    }

    public UUID getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public StoreRole getRole() { return role; }
    public boolean isUserActive() { return userActive; }
    public boolean isActive() { return active; }
    public Instant getJoinedAt() { return joinedAt; }
}
