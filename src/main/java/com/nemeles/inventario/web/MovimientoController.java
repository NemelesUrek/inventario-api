package com.nemeles.inventario.web;

import com.nemeles.inventario.service.ComprobanteService;
import com.nemeles.inventario.service.ProductoService;
import com.nemeles.inventario.web.dto.MovimientoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ComprobanteService comprobantes;

    public MovimientoController(ProductoService service, ComprobanteService comprobantes) {
        this.service = service;
        this.comprobantes = comprobantes;
    }

    @GetMapping
    @Operation(summary = "Movimientos recientes (global)",
            description = "Los últimos 100 movimientos de todo el inventario, del más nuevo al más viejo.")
    public List<MovimientoResponse> recientes() {
        return service.movimientosRecientes().stream().map(MovimientoResponse::de).toList();
    }

    @GetMapping(value = "/{id}/comprobante.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Comprobante PDF de un movimiento",
            description = "Documento contable numerado, generado en el servidor a partir del movimiento.")
    public ResponseEntity<byte[]> comprobante(@PathVariable Long id) {
        byte[] pdf = comprobantes.generar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"comprobante-" + id + ".pdf\"")
                .body(pdf);
    }
}
