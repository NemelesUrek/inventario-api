package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.Usuario;
import com.nemeles.inventario.repo.UsuarioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Gestión de usuarios/trabajadores. Las contraseñas y PIN se guardan cifrados (BCrypt). */
@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoria;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder, AuditoriaService auditoria) {
        this.repo = repo;
        this.encoder = encoder;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repo.findAll(Sort.by("username"));
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("No existe el usuario con id " + id));
    }

    @Transactional
    public Usuario crear(String username, String nombre, Usuario.Rol rol, String password, String pin) {
        if (repo.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Ya existe un usuario con el nombre '" + username + "'.");
        }
        Usuario u = new Usuario(username, encoder.encode(password), nombre, rol);
        if (StringUtils.hasText(pin)) {
            u.setPinHash(encoder.encode(pin));
        }
        u = repo.save(u);
        auditoria.registrar("CREAR", "USUARIO", u.getId(), "Alta de usuario " + username + " (" + rol + ")");
        return u;
    }

    @Transactional
    public Usuario actualizar(Long id, String nombre, Usuario.Rol rol, Boolean activo, String password, String pin) {
        Usuario u = obtener(id);
        boolean eraAdminActivo = u.getRol() == Usuario.Rol.ADMINISTRADOR && u.isActivo();
        boolean seraAdminActivo = rol == Usuario.Rol.ADMINISTRADOR && (activo != null ? activo : u.isActivo());
        if (eraAdminActivo && !seraAdminActivo && adminActivos() <= 1) {
            throw new ConflictException("Debe quedar al menos un administrador activo.");
        }
        u.setNombre(nombre);
        u.setRol(rol);
        if (activo != null) {
            u.setActivo(activo);
        }
        if (StringUtils.hasText(password)) {
            u.setPasswordHash(encoder.encode(password));
        }
        if (StringUtils.hasText(pin)) {
            u.setPinHash(encoder.encode(pin));
        }
        u = repo.save(u);
        auditoria.registrar("ACTUALIZAR", "USUARIO", u.getId(), "Actualizó al usuario " + u.getUsername());
        return u;
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario u = obtener(id);
        if (u.getUsername().equalsIgnoreCase(usuarioActual())) {
            throw new ConflictException("No puedes eliminar tu propia cuenta.");
        }
        if (u.getRol() == Usuario.Rol.ADMINISTRADOR && u.isActivo() && adminActivos() <= 1) {
            throw new ConflictException("Debe quedar al menos un administrador.");
        }
        repo.delete(u);
        auditoria.registrar("ELIMINAR", "USUARIO", id, "Eliminó al usuario " + u.getUsername());
    }

    private long adminActivos() {
        return repo.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.ADMINISTRADOR && u.isActivo())
                .count();
    }

    private String usuarioActual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}
