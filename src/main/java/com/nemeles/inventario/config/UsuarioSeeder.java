package com.nemeles.inventario.config;

import com.nemeles.inventario.domain.Usuario;
import com.nemeles.inventario.repo.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crea usuarios de ejemplo (uno por rol) la primera vez, para poder iniciar sesión. */
@Component
public class UsuarioSeeder implements CommandLineRunner {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioSeeder(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }
        repo.save(new Usuario("admin", encoder.encode("admin123"), "Administrador", Usuario.Rol.ADMINISTRADOR));
        repo.save(new Usuario("almacen", encoder.encode("almacen123"), "Encargado de Almacén", Usuario.Rol.ALMACEN));
        repo.save(new Usuario("compras", encoder.encode("compras123"), "Encargado de Compras", Usuario.Rol.COMPRAS));
        repo.save(new Usuario("ventas", encoder.encode("ventas123"), "Encargado de Ventas", Usuario.Rol.VENTAS));
    }
}
