package com.nemeles.inventario.repo;

import com.nemeles.inventario.domain.AdjuntoMovimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdjuntoRepository extends JpaRepository<AdjuntoMovimiento, Long> {

    List<AdjuntoMovimiento> findByMovimientoIdOrderByFechaAsc(Long movimientoId);

    long countByMovimientoId(Long movimientoId);
}
