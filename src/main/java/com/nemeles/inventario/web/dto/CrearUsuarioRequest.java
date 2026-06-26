package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo para dar de alta un usuario (trabajador). El PIN es opcional. */
public record CrearUsuarioRequest(
        @NotBlank @Size(max = 60) String username,
        @NotBlank @Size(max = 120) String nombre,
        @NotNull Usuario.Rol rol,
        @NotBlank @Size(min = 6, max = 72) String password,
        @Size(min = 4, max = 8) String pin
) {
}
