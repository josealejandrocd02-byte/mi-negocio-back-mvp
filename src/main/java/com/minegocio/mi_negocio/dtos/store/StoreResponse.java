package com.minegocio.mi_negocio.dtos.store;

import java.time.Instant;
import java.util.UUID;

import com.minegocio.mi_negocio.emus.StoreStatus;

public class StoreResponse {

    private final UUID id;
    private final String name;
    private final String address;
    private final String phone;
    private final String logoUrl;
    private final StoreStatus status;
    private final UUID ownerId;
    private final String ownerName;
    private final String ownerEmail;
    private final Instant createdAt;

    public StoreResponse(UUID id, String name, String address, String phone, String logoUrl,
                         StoreStatus status, UUID ownerId, String ownerName, String ownerEmail,
                         Instant createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.logoUrl = logoUrl;
        this.status = status;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getLogoUrl() { return logoUrl; }
    public StoreStatus getStatus() { return status; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerEmail() { return ownerEmail; }
    public Instant getCreatedAt() { return createdAt; }
}
