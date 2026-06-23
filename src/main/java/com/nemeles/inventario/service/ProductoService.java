package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.Movimiento;
import com.nemeles.inventario.domain.Producto;
import com.nemeles.inventario.repo.MovimientoRepository;
import com.nemeles.inventario.repo.ProductoRepository;
import com.nemeles.inventario.web.dto.StatsResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Lógica de inventario. Cada cambio de stock se persiste junto con su registro de
 * auditoría dentro de una transacción ({@code @Transactional}): o se guardan ambos, o ninguno.
 */
@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final MovimientoRepository movimientos;

    public ProductoService(ProductoRepository productos, MovimientoRepository movimientos) {
        this.productos = productos;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public Page<Producto> listar(String buscar, Pageable pageable) {
        if (StringUtils.hasText(buscar)) {
            return productos.findByNombreContainingIgnoreCaseOrSkuContainingIgnoreCase(buscar, buscar, pageable);
        }
        return productos.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Producto obtener(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el producto con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Producto> bajoStock() {
        return productos.findBajoStock();
    }

    @Transactional(readOnly = true)
    public List<Movimiento> movimientos(Long productoId) {
        obtener(productoId); // valida que exista (404 si no)
        return movimientos.findByProductoIdOrderByFechaDesc(productoId);
    }

    @Transactional(readOnly = true)
    public List<Movimiento> movimientosRecientes() {
        return movimientos.findTop100ByOrderByFechaDescIdDesc();
    }

    @Transactional(readOnly = true)
    public StatsResponse estadisticas() {
        List<Producto> todos = productos.findAll();
        long unidades = todos.stream().mapToLong(Producto::getStock).sum();
        long valor = todos.stream().mapToLong(p -> p.getPrecioCentavos() * p.getStock()).sum();
        long bajo = todos.stream().filter(Producto::isBajoStock).count();
        return new StatsResponse(
                todos.size(),
                unidades,
                valor,
                String.format(Locale.US, "$%,.2f", valor / 100.0),
                bajo,
                movimientos.count(),
                movimientos.countByTipo(Movimiento.Tipo.ENTRADA),
                movimientos.countByTipo(Movimiento.Tipo.SALIDA));
    }

    @Transactional
    public Producto crear(String sku, String nombre, String descripcion,
                          long precioCentavos, int stockInicial, int stockMinimo) {
        if (productos.existsBySkuIgnoreCase(sku)) {
            throw new ConflictException("Ya existe un producto con el SKU '" + sku + "'");
        }
        Producto p = productos.save(new Producto(sku, nombre, descripcion, precioCentavos, stockInicial, stockMinimo));
        if (stockInicial > 0) {
            movimientos.save(new Movimiento(p, Movimiento.Tipo.ENTRADA, stockInicial, "Alta inicial"));
        }
        return p;
    }

    @Transactional
    public Producto actualizar(Long id, String nombre, String descripcion, long precioCentavos, int stockMinimo) {
        Producto p = obtener(id);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecioCentavos(precioCentavos);
        p.setStockMinimo(stockMinimo);
        return productos.save(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = obtener(id);
        productos.delete(p);
    }

    @Transactional
    public Producto entrada(Long id, int cantidad, String motivo) {
        Producto p = obtener(id);
        p.setStock(p.getStock() + cantidad);
        productos.save(p);
        movimientos.save(new Movimiento(p, Movimiento.Tipo.ENTRADA, cantidad, motivo));
        return p;
    }

    @Transactional
    public Producto salida(Long id, int cantidad, String motivo) {
        Producto p = obtener(id);
        if (cantidad > p.getStock()) {
            throw new ConflictException(
                    "Stock insuficiente: hay " + p.getStock() + " unidades y se intentan retirar " + cantidad);
        }
        p.setStock(p.getStock() - cantidad);
        productos.save(p);
        movimientos.save(new Movimiento(p, Movimiento.Tipo.SALIDA, cantidad, motivo));
        return p;
    }
}
