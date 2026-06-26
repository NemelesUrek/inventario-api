package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.Proveedor;
import java.time.Instant;

/** Vista de un proveedor que devuelve la API. */
public record ProveedorResponse(
        Long id,
        String nombre,
        String contacto,
        String telefono,
        String email,
        String rfc,
        String notas,
        boolean activo,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static ProveedorResponse de(Proveedor p) {
        return new ProveedorResponse(
                p.getId(), p.getNombre(), p.getContacto(), p.getTelefono(),
                p.getEmail(), p.getRfc(), p.getNotas(), p.isActivo(),
                p.getCreadoEn(), p.getActualizadoEn());
    }
}
