package com.nemeles.inventario.web;

import com.nemeles.inventario.domain.AdjuntoMovimiento;
import com.nemeles.inventario.service.AdjuntoService;
import com.nemeles.inventario.web.dto.AdjuntoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Adjuntos", description = "Fotos y firmas de los movimientos de stock")
@RestController
@RequestMapping("/api/movimientos/{movId}/adjuntos")
public class AdjuntoController {

    private final AdjuntoService service;

    public AdjuntoController(AdjuntoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar adjuntos de un movimiento")
    public List<AdjuntoResponse> listar(@PathVariable Long movId) {
        return service.listar(movId).stream().map(AdjuntoResponse::de).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Adjuntar una foto/firma a un movimiento (multipart)")
    public ResponseEntity<AdjuntoResponse> subir(@PathVariable Long movId,
                                                 @RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "productoId", required = false) Long productoId,
                                                 @RequestParam(value = "tipo", defaultValue = "FOTO") AdjuntoMovimiento.Tipo tipo,
                                                 UriComponentsBuilder uri) {
        AdjuntoMovimiento a = service.guardar(movId, productoId, file, tipo);
        URI location = uri.path("/api/movimientos/{m}/adjuntos/{id}").buildAndExpand(movId, a.getId()).toUri();
        return ResponseEntity.created(location).body(AdjuntoResponse.de(a));
    }

    @GetMapping("/{adjId}")
    @Operation(summary = "Servir la imagen de un adjunto (autenticado)")
    public ResponseEntity<Resource> ver(@PathVariable Long movId, @PathVariable Long adjId) {
        AdjuntoMovimiento a = service.obtener(movId, adjId);
        byte[] bytes = service.leerBytes(a);
        // Se sirve como descarga (attachment) y con nosniff para que el navegador nunca interprete el contenido inline.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + a.getNombreAlmacenado() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(a.getContentType()))
                .body(new ByteArrayResource(bytes));
    }

    @DeleteMapping("/{adjId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un adjunto")
    public void eliminar(@PathVariable Long movId, @PathVariable Long adjId) {
        service.eliminar(movId, adjId);
    }
}
