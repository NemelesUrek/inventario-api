package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.Proveedor;
import com.nemeles.inventario.repo.ProveedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** CRUD de proveedores. */
@Service
public class ProveedorService {

    private final ProveedorRepository repo;
    private final AuditoriaService auditoria;

    public ProveedorService(ProveedorRepository repo, AuditoriaService auditoria) {
        this.repo = repo;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<Proveedor> listar(String buscar, Pageable pageable) {
        if (StringUtils.hasText(buscar)) {
            return repo.findByNombreContainingIgnoreCaseOrContactoContainingIgnoreCase(buscar, buscar, pageable);
        }
        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Proveedor obtener(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el proveedor con id " + id));
    }

    @Transactional
    public Proveedor crear(String nombre, String contacto, String telefono, String email, String rfc, String notas) {
        Proveedor p = repo.save(new Proveedor(nombre, contacto, telefono, email, rfc, notas));
        auditoria.registrar("CREAR", "PROVEEDOR", p.getId(), "Alta de proveedor " + p.getNombre());
        return p;
    }

    @Transactional
    public Proveedor actualizar(Long id, String nombre, String contacto, String telefono,
                                String email, String rfc, String notas, Boolean activo) {
        Proveedor p = obtener(id);
        p.setNombre(nombre);
        p.setContacto(contacto);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setRfc(rfc);
        p.setNotas(notas);
        if (activo != null) {
            p.setActivo(activo);
        }
        Proveedor guardado = repo.save(p);
        auditoria.registrar("ACTUALIZAR", "PROVEEDOR", guardado.getId(), "Actualizó proveedor " + guardado.getNombre());
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        Proveedor p = obtener(id);
        repo.delete(p);
        auditoria.registrar("ELIMINAR", "PROVEEDOR", id, "Eliminó proveedor " + p.getNombre());
    }
}
