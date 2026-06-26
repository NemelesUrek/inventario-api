package com.nemeles.inventario.web;

import com.nemeles.inventario.domain.Proveedor;
import com.nemeles.inventario.service.ProveedorService;
import com.nemeles.inventario.web.dto.ActualizarProveedorRequest;
import com.nemeles.inventario.web.dto.CrearProveedorRequest;
import com.nemeles.inventario.web.dto.ProveedorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Proveedores", description = "Catálogo de proveedores: a quién se le compra el inventario")
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService service;

    public ProveedorController(ProveedorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores", description = "Con búsqueda opcional (?buscar=) por nombre o contacto, y paginación.")
    public Page<ProveedorResponse> listar(
            @RequestParam(required = false) String buscar,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return service.listar(buscar, pageable).map(ProveedorResponse::de);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un proveedor por id")
    public ProveedorResponse obtener(@PathVariable Long id) {
        return ProveedorResponse.de(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear proveedor", description = "Devuelve 201 con la cabecera Location.")
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody CrearProveedorRequest req,
                                                   UriComponentsBuilder uri) {
        Proveedor p = service.crear(req.nombre(), req.contacto(), req.telefono(), req.email(), req.rfc(), req.notas());
        URI location = uri.path("/api/proveedores/{id}").buildAndExpand(p.getId()).toUri();
        return ResponseEntity.created(location).body(ProveedorResponse.de(p));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un proveedor")
    public ProveedorResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProveedorRequest req) {
        return ProveedorResponse.de(service.actualizar(id, req.nombre(), req.contacto(), req.telefono(),
                req.email(), req.rfc(), req.notas(), req.activo()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un proveedor")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
