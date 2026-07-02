package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.Usuario;
import com.nemeles.inventario.repo.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Carga los usuarios desde la base de datos para Spring Security. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public UsuarioDetailsService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario u = repo.findByUsernameIgnoreCase(username)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        return User.withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles(u.getRol().name())
                .build();
    }
}
