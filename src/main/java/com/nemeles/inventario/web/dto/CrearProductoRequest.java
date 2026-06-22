package com.nemeles.inventario.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Cuerpo para crear un producto. */
public record CrearProductoRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 500) String descripcion,
        @PositiveOrZero long precioCentavos,
        @PositiveOrZero int stockInicial,
        @PositiveOrZero int stockMinimo
) {
}
