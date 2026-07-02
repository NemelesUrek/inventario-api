package com.nemeles.inventario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Proveedor de una PYME: a quién se le compra el inventario.
 * Módulo de Operación; se vincula a productos en un paso posterior.
 */
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    /** Persona de contacto (opcional). */
    @Column(length = 120)
    private String contacto;

    @Column(length = 40)
    private String telefono;

    @Column(length = 120)
    private String email;

    /** RFC para facturación (contexto mexicano). */
    @Column(length = 20)
    private String rfc;

    @Column(length = 500)
    private String notas;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    @Column(nullable = false)
    private Instant actualizadoEn = Instant.now();

    protected Proveedor() {
        // requerido por JPA
    }

    public Proveedor(String nombre, String contacto, String telefono, String email, String rfc, String notas) {
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.rfc = rfc;
        this.notas = notas;
    }

    @PreUpdate
    void alActualizar() {
        this.actualizadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
