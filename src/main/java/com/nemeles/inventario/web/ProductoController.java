package com.nemeles.inventario.web;

import com.nemeles.inventario.domain.Producto;
import com.nemeles.inventario.service.ProductoFotoService;
import com.nemeles.inventario.service.ProductoService;
import com.nemeles.inventario.web.dto.ActualizarProductoRequest;
import com.nemeles.inventario.web.dto.AjusteStockRequest;
import com.nemeles.inventario.web.dto.CrearProductoRequest;
import com.nemeles.inventario.web.dto.MovimientoResponse;
import com.nemeles.inventario.web.dto.ProductoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Productos", description = "Control de inventario: productos, stock y auditoría de movimientos")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;
    private final ProductoFotoService fotos;

    public ProductoController(ProductoService service, ProductoFotoService fotos) {
        this.service = service;
        this.fotos = fotos;
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = "Con búsqueda opcional (?buscar=) por nombre o SKU, y paginación.")
    public Page<ProductoResponse> listar(
            @RequestParam(required = false) String buscar,
            @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return service.listar(buscar, pageable).map(ProductoResponse::de);
    }

    @GetMapping("/bajo-stock")
    @Operation(summary = "Alertas de reposición", description = "Productos con stock en o por debajo del mínimo.")
    public List<ProductoResponse> bajoStock() {
        return service.bajoStock().stream().map(ProductoResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por id")
    public ProductoResponse obtener(@PathVariable Long id) {
        return ProductoResponse.de(service.obtener(id));
    }

    @PostMapping
    @Operation(summary = "Crear producto", description = "Devuelve 201 con la cabecera Location. 409 si el SKU ya existe.")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CrearProductoRequest req,
                                                  UriComponentsBuilder uri) {
        Producto p = service.crear(req.sku(), req.nombre(), req.descripcion(),
                req.precioCentavos(), req.stockInicial(), req.stockMinimo(), req.categoria(), req.codigoBarras());
        URI location = uri.path("/api/productos/{id}").buildAndExpand(p.getId()).toUri();
        return ResponseEntity.created(location).body(ProductoResponse.de(p));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un producto")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarProductoRequest req) {
        return ProductoResponse.de(service.actualizar(id, req.nombre(), req.descripcion(),
                req.precioCentavos(), req.stockMinimo(), req.categoria(), req.codigoBarras()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un producto")
    public void eliminar(@PathVariable Long id) {
        fotos.borrarArchivo(service.obtener(id).getFotoNombre()); // no dejar la foto huérfana en disco
        service.eliminar(id);
    }

    @PostMapping("/{id}/entrada")
    @Operation(summary = "Entrada de stock", description = "Suma unidades y deja registro de auditoría.")
    public ProductoResponse entrada(@PathVariable Long id, @Valid @RequestBody AjusteStockRequest req) {
        return ProductoResponse.de(service.entrada(id, req.cantidad(), req.motivo(), req.pin(), req.firma()));
    }

    @PostMapping("/{id}/salida")
    @Operation(summary = "Salida de stock", description = "Resta unidades. Devuelve 409 si no hay stock suficiente.")
    public ProductoResponse salida(@PathVariable Long id, @Valid @RequestBody AjusteStockRequest req) {
        return ProductoResponse.de(service.salida(id, req.cantidad(), req.motivo(), req.pin(), req.firma()));
    }

    @GetMapping("/{id}/movimientos")
    @Operation(summary = "Historial de movimientos", description = "Auditoría de entradas y salidas del producto.")
    public List<MovimientoResponse> movimientos(@PathVariable Long id) {
        return service.movimientos(id).stream().map(MovimientoResponse::de).toList();
    }

    @PostMapping(path = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir/reemplazar la foto del producto (multipart)")
    public ProductoResponse subirFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ProductoResponse.de(fotos.guardar(service.obtener(id), file));
    }

    @GetMapping("/{id}/foto")
    @Operation(summary = "Servir la foto del producto")
    public ResponseEntity<Resource> verFoto(@PathVariable Long id) {
        Producto p = service.obtener(id);
        byte[] bytes = fotos.leerBytes(p);
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noCache())
                .contentType(MediaType.parseMediaType(fotos.contentType(p)))
                .body(new ByteArrayResource(bytes));
    }

    @DeleteMapping("/{id}/foto")
    @Operation(summary = "Quitar la foto del producto")
    public ProductoResponse eliminarFoto(@PathVariable Long id) {
        return ProductoResponse.de(fotos.eliminar(service.obtener(id)));
    }
}
