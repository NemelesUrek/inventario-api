package com.nemeles.inventario.repo;

import com.nemeles.inventario.domain.Movimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByProductoIdOrderByFechaDesc(Long productoId);
}
