package com.nemeles.inventario.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Cuerpo para actualizar los datos de un producto (no cambia el stock; eso se hace con entrada/salida). */
public record ActualizarProductoRequest(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 500) String descripcion,
        @PositiveOrZero @Max(100_000_000) long precioCentavos,
        @PositiveOrZero @Max(1_000_000) int stockMinimo,
        @Size(max = 80) String categoria
) {
}
