package com.nemeles.inventario.einvoicing.api;

/**
 * País fiscal del comprobante. Permite que el modelo y el controlador sean
 * neutros: cada país se resuelve a su propio {@link FacturacionElectronicaProvider}
 * (México hoy; Colombia, Chile, Argentina y España quedan como extensión futura).
 */
public enum Pais {
    /** México — CFDI 4.0 vía PAC (Facturama). */
    MX,
    /** Colombia — factura electrónica DIAN. TODO: proveedor pendiente. */
    CO,
    /** Chile — DTE / SII. TODO: proveedor pendiente. */
    CL,
    /** Argentina — factura electrónica AFIP. TODO: proveedor pendiente. */
    AR,
    /** España — Facturae / Verifactu. TODO: proveedor pendiente. */
    ES
}
