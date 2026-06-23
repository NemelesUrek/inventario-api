package com.nemeles.inventario.service;

import com.nemeles.inventario.repo.MovimientoRepository;
import com.nemeles.inventario.repo.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Datos de ejemplo de la demo (la BD es en memoria). Centraliza la siembra para que
 * la pueda usar tanto el arranque ({@code DataSeeder}) como el endpoint de reinicio.
 */
@Service
public class DemoDataService {

    private final ProductoService service;
    private final ProductoRepository productos;
    private final MovimientoRepository movimientos;

    public DemoDataService(ProductoService service, ProductoRepository productos, MovimientoRepository movimientos) {
        this.service = service;
        this.productos = productos;
        this.movimientos = movimientos;
    }

    @Transactional
    public void seedIfEmpty() {
        if (productos.count() == 0) {
            seed();
        }
    }

    /** Borra todo y vuelve a sembrar los datos de ejemplo. Devuelve cuántos productos quedaron. */
    @Transactional
    public long reset() {
        movimientos.deleteAll();
        productos.deleteAll();
        seed();
        return productos.count();
    }

    private void seed() {
        service.crear("CAF-001", "Café de altura 1 kg", "Grano arábica, tueste medio", 18900, 40, 10);
        service.crear("TAZ-114", "Taza de cerámica 350 ml", "Color mate, apta para microondas", 7950, 8, 12);   // bajo stock
        service.crear("AUR-220", "Audífonos inalámbricos", "Bluetooth 5.3, 24 h de batería", 49900, 25, 5);
        service.crear("CUA-007", "Cuaderno A5 punteado", "120 hojas, tapa dura", 5990, 120, 20);
        service.crear("MOC-330", "Mochila urbana 20 L", "Resistente al agua, puerto USB", 64900, 4, 6);          // bajo stock
        service.crear("BOT-410", "Botella térmica 750 ml", "Acero inoxidable, 12 h frío", 21500, 60, 15);
    }
}
