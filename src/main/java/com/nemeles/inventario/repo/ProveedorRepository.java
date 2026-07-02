package com.nemeles.inventario.repo;

import com.nemeles.inventario.domain.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Page<Proveedor> findByNombreContainingIgnoreCaseOrContactoContainingIgnoreCase(
            String nombre, String contacto, Pageable pageable);
}
