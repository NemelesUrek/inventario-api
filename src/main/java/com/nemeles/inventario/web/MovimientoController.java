package com.nemeles.inventario.web;

import com.nemeles.inventario.service.ProductoService;
import com.nemeles.inventario.web.dto.MovimientoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Feed global de auditoría: todos los movimientos de stock del inventario,
 * los más recientes primero. Útil para una vista de "actividad" transversal.
 */
@Tag(name = "Movimientos", description = "Auditoría global de entradas y salidas de stock")
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final ProductoService service;

    public MovimientoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Movimientos recientes (global)",
            description = "Los últimos 100 movimientos de todo el inventario, del más nuevo al más viejo.")
    public List<MovimientoResponse> recientes() {
        return service.movimientosRecientes().stream().map(MovimientoResponse::de).toList();
    }
}
