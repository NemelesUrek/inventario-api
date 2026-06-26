package com.nemeles.inventario.repo;

import com.nemeles.inventario.domain.RegistroAuditoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    /** Bitácora: los registros más recientes primero. */
    List<RegistroAuditoria> findTop200ByOrderByFechaDescIdDesc();
}
