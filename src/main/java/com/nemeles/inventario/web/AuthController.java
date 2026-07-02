package com.nemeles.inventario.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sesión. El inicio (POST /api/auth/login) y cierre (POST /api/auth/logout)
 * los maneja Spring Security; aquí se expone el usuario actual.
 */
@Tag(name = "Autenticación", description = "Inicio de sesión y usuario actual")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Usuario actual", description = "Devuelve el usuario y sus roles; 401 si no hay sesión.")
    public Map<String, Object> me(Authentication auth) {
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .toList();
        return Map.of("username", auth.getName(), "roles", roles);
    }
}
