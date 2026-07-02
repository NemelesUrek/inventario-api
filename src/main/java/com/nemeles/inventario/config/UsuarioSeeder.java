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
        String pin = encoder.encode("1234"); // PIN de ejemplo para todos (demo)
        crear("admin", "admin123", "Administrador", Usuario.Rol.ADMINISTRADOR, pin);
        crear("almacen", "almacen123", "Encargado de Almacén", Usuario.Rol.ALMACEN, pin);
        crear("compras", "compras123", "Encargado de Compras", Usuario.Rol.COMPRAS, pin);
        crear("ventas", "ventas123", "Encargado de Ventas", Usuario.Rol.VENTAS, pin);
    }

    private void crear(String username, String pass, String nombre, Usuario.Rol rol, String pinHash) {
        Usuario u = new Usuario(username, encoder.encode(pass), nombre, rol);
        u.setPinHash(pinHash);
        repo.save(u);
    }
}
