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
 * Registro de auditoría de cada movimiento de stock (append-only).
 * Cada entrada/salida deja huella: tipo, cantidad, stock resultante, motivo y fecha.
 */
@Entity
@Table(name = "movimientos", indexes = @Index(name = "idx_mov_producto", columnList = "productoId"))
public class Movimiento {

    public enum Tipo { ENTRADA, SALIDA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Tipo tipo;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int stockResultante;

    @Column(length = 255)
    private String motivo;

    @Column(nullable = false, updatable = false)
    private Instant fecha = Instant.now();

    /** Confirmación del movimiento (PIN / firma). Opcional. */
    @Column(length = 80)
    private String confirmadoPor;
    private Instant confirmadoEn;
    @Column(length = 20)
    private String metodoConfirmacion;

    protected Movimiento() {
        // requerido por JPA
    }

    public Movimiento(Producto producto, Tipo tipo, int cantidad, String motivo) {
        this.productoId = producto.getId();
        this.sku = producto.getSku();
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockResultante = producto.getStock();
        this.motivo = motivo;
    }

    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public String getSku() { return sku; }
    public Tipo getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public int getStockResultante() { return stockResultante; }
    public String getMotivo() { return motivo; }
    public Instant getFecha() { return fecha; }
    public String getConfirmadoPor() { return confirmadoPor; }
    public void setConfirmadoPor(String confirmadoPor) { this.confirmadoPor = confirmadoPor; }
    public Instant getConfirmadoEn() { return confirmadoEn; }
    public void setConfirmadoEn(Instant confirmadoEn) { this.confirmadoEn = confirmadoEn; }
    public String getMetodoConfirmacion() { return metodoConfirmacion; }
    public void setMetodoConfirmacion(String metodoConfirmacion) { this.metodoConfirmacion = metodoConfirmacion; }
}
