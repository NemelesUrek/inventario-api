package com.nemeles.inventario.web.dto;

/**
 * Resumen agregado del inventario, calculado en el servidor.
 * Pensado para alimentar dashboards sin que el cliente tenga que recorrer todo el catálogo.
 */
public record StatsResponse(
        long totalProductos,
        long unidadesTotales,
        long valorInventarioCentavos,
        String valorInventario,
        long productosBajoStock,
        long totalMovimientos,
        long entradas,
        long salidas
) {
}
