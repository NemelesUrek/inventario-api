package com.nemeles.inventario.web;

import com.nemeles.inventario.domain.Usuario;
import com.nemeles.inventario.service.UsuarioService;
import com.nemeles.inventario.web.dto.ActualizarUsuarioRequest;
import com.nemeles.inventario.web.dto.CrearUsuarioRequest;
import com.nemeles.inventario.web.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Usuarios", description = "Gestión de usuarios y roles (solo Administrador)")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<UsuarioResponse> listar() {
        return service.listar().stream().map(UsuarioResponse::de).toList();
    }

    @PostMapping
    @Operation(summary = "Crear usuario (trabajador)")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest req, UriComponentsBuilder uri) {
        Usuario u = service.crear(req.username(), req.nombre(), req.rol(), req.password(), req.pin());
        URI location = uri.path("/api/usuarios/{id}").buildAndExpand(u.getId()).toUri();
        return ResponseEntity.created(location).body(UsuarioResponse.de(u));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario (nombre, rol, activo; opcional contraseña/PIN)")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest req) {
        return UsuarioResponse.de(service.actualizar(id, req.nombre(), req.rol(), req.activo(), req.password(), req.pin()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
