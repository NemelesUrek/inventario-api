package com.nemeles.inventario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Adjunto (foto o firma) de un movimiento de stock. El binario se guarda en disco
 * (carpeta de datos de la app), NO como BLOB en la base; aquí solo va la referencia.
 */
@Entity
@Table(name = "adjuntos_movimiento", indexes = @Index(name = "idx_adj_mov", columnList = "movimientoId"))
public class AdjuntoMovimiento {

    public enum Tipo { FOTO, FIRMA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long movimientoId;

    private Long productoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tipo tipo;

    /** Nombre del archivo en disco (UUID + extensión). */
    @Column(nullable = false, length = 80)
    private String nombreAlmacenado;

    @Column(length = 255)
    private String nombreOriginal;

    @Column(nullable = false, length = 60)
    private String contentType;

    @Column(nullable = false)
    private long tamanoBytes;

    @Column(nullable = false, updatable = false)
    private Instant fecha = Instant.now();

    protected AdjuntoMovimiento() {
        // requerido por JPA
    }

    public AdjuntoMovimiento(Long movimientoId, Long productoId, Tipo tipo, String nombreAlmacenado,
                             String nombreOriginal, String contentType, long tamanoBytes) {
        this.movimientoId = movimientoId;
        this.productoId = productoId;
        this.tipo = tipo;
        this.nombreAlmacenado = nombreAlmacenado;
        this.nombreOriginal = nombreOriginal;
        this.contentType = contentType;
        this.tamanoBytes = tamanoBytes;
    }

    public Long getId() { return id; }
    public Long getMovimientoId() { return movimientoId; }
    public Long getProductoId() { return productoId; }
    public Tipo getTipo() { return tipo; }
    public String getNombreAlmacenado() { return nombreAlmacenado; }
    public String getNombreOriginal() { return nombreOriginal; }
    public String getContentType() { return contentType; }
    public long getTamanoBytes() { return tamanoBytes; }
    public Instant getFecha() { return fecha; }
}
