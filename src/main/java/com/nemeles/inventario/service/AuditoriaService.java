package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.RegistroAuditoria;
import com.nemeles.inventario.repo.AuditoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra y consulta la bitácora de auditoría. Cada {@code registrar(...)} se
 * llama dentro de la transacción de la operación que lo origina, así que el cambio
 * y su huella se guardan juntos (o ninguno).
 */
@Service
public class AuditoriaService {

    private final AuditoriaRepository repo;

    public AuditoriaService(AuditoriaRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void registrar(String accion, String entidad, Long entidadId, String descripcion) {
        repo.save(new RegistroAuditoria(accion, entidad, entidadId, descripcion, usuarioActual()));
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoria> recientes() {
        return repo.findTop200ByOrderByFechaDescIdDesc();
    }

    /** Usuario de la sesión actual; "sistema" si no hay sesión (seeders, arranque). */
    private String usuarioActual() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "sistema";
        }
        return auth.getName();
    }
}
