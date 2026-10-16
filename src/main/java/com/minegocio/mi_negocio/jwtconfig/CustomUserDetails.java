package com.minegocio.mi_negocio.jwtconfig;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.minegocio.mi_negocio.emus.PlatformRole;
import com.minegocio.mi_negocio.entity.User;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public UUID getUserId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public PlatformRole getPlatformRole() {
        return user.getPlatformRole();
    }

    // Solo rol de plataforma (ROLE_SUPER_ADMIN / ROLE_USER).
    // STORE_ADMIN y EMPLOYEE dependen de la sucursal y se validan en los servicios.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getPlatformRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    // El login es por email
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
