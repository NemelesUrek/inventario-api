package com.nemeles.inventario.config;

import com.nemeles.inventario.repo.ProductoRepository;
import com.nemeles.inventario.service.ProductoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga datos de ejemplo al arrancar (la BD es en memoria). Incluye un par de productos
 * por debajo del mínimo para que la alerta de reposición sea visible en la demo.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductoService service;
    private final ProductoRepository productos;

    public DataSeeder(ProductoService service, ProductoRepository productos) {
        this.service = service;
        this.productos = productos;
    }

    @Override
    public void run(String... args) {
        if (productos.count() > 0) {
            return;
        }
        service.crear("CAF-001", "Café de altura 1 kg", "Grano arábica, tueste medio", 18900, 40, 10);
        service.crear("TAZ-114", "Taza de cerámica 350 ml", "Color mate, apta para microondas", 7950, 8, 12);   // bajo stock
        service.crear("AUR-220", "Audífonos inalámbricos", "Bluetooth 5.3, 24 h de batería", 49900, 25, 5);
        service.crear("CUA-007", "Cuaderno A5 punteado", "120 hojas, tapa dura", 5990, 120, 20);
        service.crear("MOC-330", "Mochila urbana 20 L", "Resistente al agua, puerto USB", 64900, 4, 6);          // bajo stock
        service.crear("BOT-410", "Botella térmica 750 ml", "Acero inoxidable, 12 h frío", 21500, 60, 15);
    }
}
