package com.nemeles.inventario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Bitácora de auditoría (append-only): quién hizo qué y cuándo sobre el inventario.
 * El "quién" es "sistema" hasta que exista el inicio de sesión.
 */
@Entity
@Table(name = "auditoria", indexes = @Index(name = "idx_aud_fecha", columnList = "fecha"))
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** CREAR, ACTUALIZAR, ELIMINAR, ENTRADA, SALIDA. */
    @Column(nullable = false, length = 20)
    private String accion;

    /** PRODUCTO, PROVEEDOR. */
    @Column(nullable = false, length = 20)
    private String entidad;

    private Long entidadId;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false, length = 80)
    private String usuario;

    @Column(nullable = false, updatable = false)
    private Instant fecha = Instant.now();

    protected RegistroAuditoria() {
        // requerido por JPA
    }

    public RegistroAuditoria(String accion, String entidad, Long entidadId, String descripcion, String usuario) {
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.descripcion = descripcion;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public Long getEntidadId() { return entidadId; }
    public String getDescripcion() { return descripcion; }
    public String getUsuario() { return usuario; }
    public Instant getFecha() { return fecha; }
}
