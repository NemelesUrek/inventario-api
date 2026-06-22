package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.Movimiento;
import java.time.Instant;

/** Vista de un movimiento de stock (registro de auditoría). */
public record MovimientoResponse(
        Long id,
        Long productoId,
        String sku,
        Movimiento.Tipo tipo,
        int cantidad,
        int stockResultante,
        String motivo,
        Instant fecha
) {
    public static MovimientoResponse de(Movimiento m) {
        return new MovimientoResponse(
                m.getId(), m.getProductoId(), m.getSku(), m.getTipo(),
                m.getCantidad(), m.getStockResultante(), m.getMotivo(), m.getFecha());
    }
}
