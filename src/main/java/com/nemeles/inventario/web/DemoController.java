package com.nemeles.inventario.web;

import com.nemeles.inventario.service.DemoDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Utilidades de la demo. La base es en memoria y cualquiera puede crear/editar/borrar,
 * así que se ofrece un reinicio para devolver el inventario a su estado de ejemplo.
 */
@Tag(name = "Demo", description = "Utilidades del entorno de demostración")
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoDataService demo;

    public DemoController(DemoDataService demo) {
        this.demo = demo;
    }

    @PostMapping("/reset")
    @Operation(summary = "Reiniciar datos de demo",
            description = "Borra todo y vuelve a sembrar el inventario de ejemplo.")
    public Map<String, Object> reset() {
        long n = demo.reset();
        return Map.of("mensaje", "Datos de demo reiniciados", "productos", n);
    }
}
