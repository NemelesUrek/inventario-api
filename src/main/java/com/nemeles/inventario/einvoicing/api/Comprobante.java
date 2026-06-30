package com.nemeles.inventario.einvoicing.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Modelo NEUTRO de un comprobante fiscal, independiente del país.
 *
 * <p>La idea es que la app (controlador, servicios) trabaje siempre con este
 * objeto; cada {@link FacturacionElectronicaProvider} lo traduce al formato que
 * exige su PAC/autoridad (en México, el JSON "Cfdi" de Facturama).</p>
 *
 * <p>Se modela con {@code record} y clases anidadas para que sea inmutable y
 * fácil de serializar con Jackson (lo recibe el controlador como JSON).</p>
 *
 * @param pais        país fiscal; decide qué proveedor lo emite
 * @param tipo        tipo de comprobante (en México: "I" ingreso, "E" egreso, etc.)
 * @param emisor      quién factura
 * @param receptor    a quién se le factura
 * @param lineas      conceptos/partidas del comprobante
 * @param subtotal    suma de importes antes de impuestos
 * @param impuestos   total de impuestos trasladados (p. ej. IVA)
 * @param total       subtotal + impuestos
 * @param moneda      código ISO de moneda (MXN, USD, COP, ...)
 * @param formaPago   forma de pago (en México: catálogo c_FormaPago, p. ej. "01" efectivo)
 * @param metodoPago  método de pago (en México: "PUE" / "PPD")
 * @param fecha       fecha de expedición; si es null el proveedor usa la actual
 */
public record Comprobante(
        Pais pais,
        String tipo,
        Emisor emisor,
        Receptor receptor,
        List<LineaComprobante> lineas,
        BigDecimal subtotal,
        BigDecimal impuestos,
        BigDecimal total,
        String moneda,
        String formaPago,
        String metodoPago,
        OffsetDateTime fecha
) {

    /**
     * Datos del emisor (quien factura).
     *
     * @param identificadorFiscal RFC en México (NIT en Colombia, etc.)
     * @param nombre              razón social
     * @param regimenFiscal       régimen fiscal (en México: catálogo c_RegimenFiscal)
     * @param lugarExpedicion     código postal del lugar de expedición (ExpeditionPlace en MX)
     */
    public record Emisor(
            String identificadorFiscal,
            String nombre,
            String regimenFiscal,
            String lugarExpedicion
    ) {
    }

    /**
     * Datos del receptor (a quien se factura).
     *
     * @param identificadorFiscal RFC del receptor
     * @param nombre              nombre/razón social tal como está en la constancia fiscal
     * @param codigoPostal        CP fiscal del receptor (TaxZipCode en MX)
     * @param regimenFiscal       régimen fiscal del receptor
     * @param usoCfdi             uso del comprobante (en México: catálogo c_UsoCFDI, p. ej. "G03")
     */
    public record Receptor(
            String identificadorFiscal,
            String nombre,
            String codigoPostal,
            String regimenFiscal,
            String usoCfdi
    ) {
    }

    /**
     * Una línea/concepto del comprobante.
     *
     * @param claveProdServ clave del producto o servicio (en México: c_ClaveProdServ del SAT)
     * @param claveUnidad   clave de unidad de medida (en México: c_ClaveUnidad, p. ej. "H87")
     * @param unidad        descripción legible de la unidad (p. ej. "Pieza")
     * @param descripcion   descripción del concepto
     * @param cantidad      cantidad
     * @param valorUnitario valor unitario antes de impuestos
     * @param importe       cantidad * valorUnitario (subtotal de la línea)
     */
    public record LineaComprobante(
            String claveProdServ,
            String claveUnidad,
            String unidad,
            String descripcion,
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal importe
    ) {
    }
}
