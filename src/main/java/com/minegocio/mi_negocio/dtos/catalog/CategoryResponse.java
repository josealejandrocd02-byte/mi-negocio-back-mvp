package com.minegocio.mi_negocio.dtos.catalog;

import java.util.UUID;

public class CategoryResponse {
    private final UUID id;
    private final String name;
    private final boolean active;

    public CategoryResponse(UUID id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
