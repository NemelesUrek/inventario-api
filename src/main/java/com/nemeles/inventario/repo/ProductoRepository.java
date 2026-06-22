package com.nemeles.inventario.repo;

import com.nemeles.inventario.domain.Producto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Page<Producto> findByNombreContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String nombre, String sku, Pageable pageable);

    /** Productos cuyo stock cayó al mínimo o por debajo (alerta de reposición). */
    @Query("select p from Producto p where p.stock <= p.stockMinimo order by p.stock asc")
    List<Producto> findBajoStock();
}
