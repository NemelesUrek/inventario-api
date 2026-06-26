package com.nemeles.inventario.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo para crear un proveedor. */
public record CrearProveedorRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 120) String contacto,
        @Size(max = 40) String telefono,
        @Email @Size(max = 120) String email,
        @Size(max = 20) String rfc,
        @Size(max = 500) String notas
) {
}
