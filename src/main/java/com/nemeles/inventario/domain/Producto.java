package com.nemeles.inventario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Producto de inventario. El precio se guarda como entero de <b>centavos</b> (long),
 * nunca como decimal de punto flotante, para evitar errores de redondeo en dinero.
 */
@Entity
@Table(name = "productos", uniqueConstraints = @UniqueConstraint(name = "uk_producto_sku", columnNames = "sku"))
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    /** Precio en centavos (ej. $199.90 = 19990). */
    @Column(nullable = false)
    private long precioCentavos;

    @Column(nullable = false)
    private int stock;

    /** Umbral para la alerta de stock bajo. */
    @Column(nullable = false)
    private int stockMinimo;

    /** Categoría opcional (p. ej. Electrónica, Hogar). Útil para filtrar y agrupar. */
    @Column(length = 80)
    private String categoria;

    /** Código de barras / QR opcional (EAN, UPC, etc.); se captura tecleando o con el escáner. */
    @Column(length = 64)
    private String codigoBarras;

    /** Nombre del archivo de la foto del producto en disco (data/productos), o null si no tiene. */
    @Column(length = 100)
    private String fotoNombre;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    @Column(nullable = false)
    private Instant actualizadoEn = Instant.now();

    protected Producto() {
        // requerido por JPA
    }

    public Producto(String sku, String nombre, String descripcion, long precioCentavos, int stock, int stockMinimo, String categoria) {
        this.sku = sku;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioCentavos = precioCentavos;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
    }

    @PreUpdate
    void alActualizar() {
        this.actualizadoEn = Instant.now();
    }

    /** true si el stock cayó al mínimo o por debajo (dispara alerta de reposición). */
    public boolean isBajoStock() {
        return stock <= stockMinimo;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public long getPrecioCentavos() { return precioCentavos; }
    public void setPrecioCentavos(long precioCentavos) { this.precioCentavos = precioCentavos; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }
    public String getFotoNombre() { return fotoNombre; }
    public void setFotoNombre(String fotoNombre) { this.fotoNombre = fotoNombre; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
