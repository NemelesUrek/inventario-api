package com.nemeles.inventario.web;

import com.nemeles.inventario.service.ProductoService;
import com.nemeles.inventario.web.dto.StatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resumen agregado del inventario, calculado en el servidor: valor total, unidades,
 * alertas y conteo de movimientos. Pensado para dashboards.
 */
@Tag(name = "Estadísticas", description = "Resumen agregado del inventario (server-side)")
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ProductoService service;

    public StatsController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Resumen del inventario",
            description = "Totales calculados en el servidor: productos, unidades, valor, alertas y movimientos.")
    public StatsResponse stats() {
        return service.estadisticas();
    }
}
