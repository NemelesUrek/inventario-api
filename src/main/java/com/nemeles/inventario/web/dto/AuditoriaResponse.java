package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.RegistroAuditoria;
import java.time.Instant;

/** Vista de un registro de auditoría que devuelve la API. */
public record AuditoriaResponse(
        Long id,
        String accion,
        String entidad,
        Long entidadId,
        String descripcion,
        String usuario,
        Instant fecha
) {
    public static AuditoriaResponse de(RegistroAuditoria r) {
        return new AuditoriaResponse(
                r.getId(), r.getAccion(), r.getEntidad(), r.getEntidadId(),
                r.getDescripcion(), r.getUsuario(), r.getFecha());
    }
}
