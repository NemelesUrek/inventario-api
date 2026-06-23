package com.nemeles.inventario.web.dto;

import com.nemeles.inventario.domain.Producto;
import java.time.Instant;

/** Vista de un producto que devuelve la API. */
public record ProductoResponse(
        Long id,
        String sku,
        String nombre,
        String descripcion,
        long precioCentavos,
        String precio,
        int stock,
        int stockMinimo,
        boolean bajoStock,
        String categoria,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static ProductoResponse de(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getPrecioCentavos(), formatear(p.getPrecioCentavos()),
                p.getStock(), p.getStockMinimo(), p.isBajoStock(),
                p.getCategoria(), p.getCreadoEn(), p.getActualizadoEn());
    }

    private static String formatear(long centavos) {
        return String.format("$%,.2f", centavos / 100.0);
    }
}
