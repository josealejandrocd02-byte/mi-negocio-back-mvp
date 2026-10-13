package com.minegocio.mi_negocio.dtos.user;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.minegocio.mi_negocio.emus.PlatformRole;

// Nunca expone passwordHash
public class UserResponse {

    private final UUID id;
    private final String fullName;
    private final String email;
    private final PlatformRole platformRole;
    private final boolean active;
    private final Instant createdAt;
    private final List<UserStoreResponse> stores;

    public UserResponse(UUID id, String fullName, String email, PlatformRole platformRole,
                        boolean active, Instant createdAt, List<UserStoreResponse> stores) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.platformRole = platformRole;
        this.active = active;
        this.createdAt = createdAt;
        this.stores = stores;
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public PlatformRole getPlatformRole() { return platformRole; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public List<UserStoreResponse> getStores() { return stores; }
}
