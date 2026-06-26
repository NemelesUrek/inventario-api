package com.nemeles.inventario.web;

import com.nemeles.inventario.service.AuditoriaService;
import com.nemeles.inventario.web.dto.AuditoriaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auditoría", description = "Bitácora append-only: quién hizo qué y cuándo")
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Bitácora reciente", description = "Los últimos 200 registros de auditoría, del más nuevo al más viejo.")
    public List<AuditoriaResponse> recientes() {
        return service.recientes().stream().map(AuditoriaResponse::de).toList();
    }
}
