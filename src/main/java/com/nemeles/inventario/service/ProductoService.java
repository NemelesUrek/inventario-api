package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.Movimiento;
import com.nemeles.inventario.domain.Producto;
import com.nemeles.inventario.domain.Usuario;
import com.nemeles.inventario.repo.MovimientoRepository;
import com.nemeles.inventario.repo.ProductoRepository;
import com.nemeles.inventario.repo.UsuarioRepository;
import com.nemeles.inventario.web.dto.StatsResponse;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final AuditoriaService auditoria;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public ProductoService(ProductoRepository productos, MovimientoRepository movimientos, AuditoriaService auditoria,
                           UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.productos = productos;
        this.movimientos = movimientos;
        this.auditoria = auditoria;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    /**
     * Valida el PIN de confirmación contra el usuario en sesión. PIN opcional: si no viene,
     * no hay confirmación (no rompe llamadas/tests sin PIN). Devuelve [usuario, metodo] o null;
     * lanza {@link PinInvalidoException} (422) si el PIN no coincide.
     */
    private String[] confirmar(String pin, String firma) {
        if (pin == null || pin.isBlank()) {
            return null;
        }
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : null;
        Usuario u = (username != null) ? usuarios.findByUsernameIgnoreCase(username).orElse(null) : null;
        if (u == null || u.getPinHash() == null || !encoder.matches(pin, u.getPinHash())) {
            throw new PinInvalidoException("El PIN de confirmación no coincide.");
        }
        return new String[]{u.getUsername(), (firma != null && !firma.isBlank()) ? "PIN+FIRMA" : "PIN"};
    }

    private void aplicarConfirmacion(Movimiento mov, String[] conf) {
        if (conf != null) {
            mov.setConfirmadoPor(conf[0]);
            mov.setConfirmadoEn(Instant.now());
            mov.setMetodoConfirmacion(conf[1]);
        }
    }

    private static String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return codigo.trim();
    }

    @Transactional(readOnly = true)
    public Page<Producto> listar(String buscar, Pageable pageable) {
        if (StringUtils.hasText(buscar)) {
            return productos.buscar(buscar, pageable);
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
                          long precioCentavos, int stockInicial, int stockMinimo, String categoria, String codigoBarras) {
        if (productos.existsBySkuIgnoreCase(sku)) {
            throw new ConflictException("Ya existe un producto con el SKU '" + sku + "'");
        }
        Producto nuevo = new Producto(sku, nombre, descripcion, precioCentavos, stockInicial, stockMinimo, categoria);
        nuevo.setCodigoBarras(normalizarCodigo(codigoBarras));
        Producto p = productos.save(nuevo);
        if (stockInicial > 0) {
            movimientos.save(new Movimiento(p, Movimiento.Tipo.ENTRADA, stockInicial, "Alta inicial"));
        }
        auditoria.registrar("CREAR", "PRODUCTO", p.getId(), "Alta de " + p.getNombre() + " (" + p.getSku() + ")");
        return p;
    }

    @Transactional
    public Producto actualizar(Long id, String nombre, String descripcion, long precioCentavos, int stockMinimo, String categoria, String codigoBarras) {
        Producto p = obtener(id);
        p.setCodigoBarras(normalizarCodigo(codigoBarras));
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecioCentavos(precioCentavos);
        p.setStockMinimo(stockMinimo);
        p.setCategoria(categoria);
        Producto guardado = productos.save(p);
        auditoria.registrar("ACTUALIZAR", "PRODUCTO", guardado.getId(), "Actualizó " + guardado.getNombre());
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = obtener(id);
        productos.delete(p);
        auditoria.registrar("ELIMINAR", "PRODUCTO", id, "Eliminó " + p.getNombre() + " (" + p.getSku() + ")");
    }

    @Transactional
    public Producto entrada(Long id, int cantidad, String motivo, String pin, String firma) {
        Producto p = obtener(id);
        String[] conf = confirmar(pin, firma);
        p.setStock(p.getStock() + cantidad);
        productos.save(p);
        Movimiento mov = new Movimiento(p, Movimiento.Tipo.ENTRADA, cantidad, motivo);
        aplicarConfirmacion(mov, conf);
        movimientos.save(mov);
        auditoria.registrar("ENTRADA", "PRODUCTO", id, "Entrada +" + cantidad + " de " + p.getSku() + " (stock " + p.getStock() + ")");
        return p;
    }

    @Transactional
    public Producto salida(Long id, int cantidad, String motivo, String pin, String firma) {
        Producto p = obtener(id);
        if (cantidad > p.getStock()) {
            throw new ConflictException(
                    "Stock insuficiente: hay " + p.getStock() + " unidades y se intentan retirar " + cantidad);
        }
        String[] conf = confirmar(pin, firma);
        p.setStock(p.getStock() - cantidad);
        productos.save(p);
        Movimiento mov = new Movimiento(p, Movimiento.Tipo.SALIDA, cantidad, motivo);
        aplicarConfirmacion(mov, conf);
        movimientos.save(mov);
        auditoria.registrar("SALIDA", "PRODUCTO", id, "Salida -" + cantidad + " de " + p.getSku() + " (stock " + p.getStock() + ")");
        return p;
    }
}
