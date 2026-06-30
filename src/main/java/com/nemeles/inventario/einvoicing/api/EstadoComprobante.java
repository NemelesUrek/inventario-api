package com.nemeles.inventario.einvoicing.api;

/**
 * Estado neutro del comprobante a lo largo de su ciclo de vida frente al PAC/autoridad.
 */
public enum EstadoComprobante {
    /** Enviado pero aún sin respuesta definitiva del PAC. */
    PENDIENTE,
    /** Timbrado/aceptado correctamente (en México: tiene UUID/folio fiscal). */
    ACEPTADO,
    /** Rechazado por la autoridad o el PAC por validación de negocio. */
    RECHAZADO,
    /** Cancelado después de haber sido aceptado. */
    CANCELADO,
    /** No se pudo comunicar con el PAC (red, credenciales, HTTP 5xx, etc.). */
    ERROR_COMUNICACION
}
