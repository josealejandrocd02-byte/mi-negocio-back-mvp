package com.minegocio.mi_negocio.dtos.user;

import jakarta.validation.constraints.NotNull;

// Sirve para bloquear/desbloquear un usuario (global) o su acceso a una sucursal
public class ActiveRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
