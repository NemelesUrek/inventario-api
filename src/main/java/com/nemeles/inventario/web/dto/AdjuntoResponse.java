package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.AdjuntoMovimiento;
import java.time.Instant;

/** Vista de un adjunto. La url sirve la imagen (autenticada) desde el controlador. */
public record AdjuntoResponse(
        Long id,
        Long movimientoId,
        AdjuntoMovimiento.Tipo tipo,
        String nombreOriginal,
        String contentType,
        long tamanoBytes,
        Instant fecha,
        String url
) {
    public static AdjuntoResponse de(AdjuntoMovimiento a) {
        return new AdjuntoResponse(
                a.getId(), a.getMovimientoId(), a.getTipo(), a.getNombreOriginal(),
                a.getContentType(), a.getTamanoBytes(), a.getFecha(),
                "/api/movimientos/" + a.getMovimientoId() + "/adjuntos/" + a.getId());
    }
}
