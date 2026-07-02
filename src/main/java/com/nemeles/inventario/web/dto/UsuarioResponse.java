package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.Usuario;
import java.time.Instant;

/** Vista de un usuario que devuelve la API. NUNCA expone el hash de contraseña ni de PIN. */
public record UsuarioResponse(
        Long id,
        String username,
        String nombre,
        Usuario.Rol rol,
        boolean activo,
        boolean tienePin,
        Instant creadoEn
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getUsername(), u.getNombre(), u.getRol(), u.isActivo(),
                u.getPinHash() != null && !u.getPinHash().isBlank(), u.getCreadoEn());
    }
}
