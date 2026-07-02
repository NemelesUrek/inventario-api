package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo para actualizar un usuario. password y pin son opcionales (solo si se cambian). */
public record ActualizarUsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotNull Usuario.Rol rol,
        Boolean activo,
        @Size(min = 6, max = 72) String password,
        @Size(min = 4, max = 8) String pin
) {
}
