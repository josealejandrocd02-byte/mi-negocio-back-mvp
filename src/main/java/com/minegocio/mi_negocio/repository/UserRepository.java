package com.minegocio.mi_negocio.repository;

import java.util.Optional; 
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.minegocio.mi_negocio.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

 // Login (UsuarioDetailsService)
 Optional<User> findByEmailIgnoreCase(String email);

 // Validar email unico al crear
 boolean existsByEmailIgnoreCase(String email);

 // Validar email unico al editar (ignorando al propio usuario)
 boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

 // Opcional: bloqueo inmediato en JWTAuthorizationFilter
 boolean existsByIdAndActiveTrue(UUID id);
}
