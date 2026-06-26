package com.nemeles.inventario.web.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cuerpo para una entrada o salida de stock. pin y firma son opcionales (confirmación). */
public record AjusteStockRequest(
        @Positive int cantidad,
        @Size(max = 255) String motivo,
        @Size(min = 4, max = 8) String pin,
        String firma
) {
}
