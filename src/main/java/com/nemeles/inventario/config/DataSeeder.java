package com.nemeles.inventario.config;

import com.nemeles.inventario.service.DemoDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga datos de ejemplo al arrancar (la BD es en memoria). La siembra real vive en
 * {@link DemoDataService} para poder reutilizarla desde el endpoint de reinicio.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final DemoDataService demo;

    public DataSeeder(DemoDataService demo) {
        this.demo = demo;
    }

    @Override
    public void run(String... args) {
        demo.seedIfEmpty();
    }
}
