package com.nemeles.inventario.einvoicing.api;

/**
 * Ambiente del PAC: pruebas (no fiscalmente válido) o producción (timbra de verdad).
 * Se decide por la URL base configurada; este enum sirve para dejarlo explícito
 * en la configuración y en los logs.
 */
public enum Ambiente {
    /** Sandbox / pruebas. Los comprobantes NO tienen validez fiscal. */
    SANDBOX,
    /** Producción. Los comprobantes timbrados SÍ tienen validez fiscal. */
    PRODUCCION
}
