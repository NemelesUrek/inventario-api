package com.nemeles.inventario.einvoicing.api;

/**
 * Resultado NEUTRO de una operación de facturación electrónica (emitir, cancelar,
 * consultar). Igual que {@link Comprobante}, no depende del país: cada proveedor
 * rellena lo que aplique y deja en {@code null} lo que no.
 *
 * @param estado            estado del comprobante tras la operación
 * @param identificadorFiscal folio fiscal / UUID (en México: el UUID del timbre)
 * @param xmlBase64         XML del comprobante codificado en Base64 (si el PAC lo devuelve)
 * @param pdfBase64         representación impresa (PDF) en Base64 (si se solicitó)
 * @param sello             sello digital del comprobante (CfdiSign en Facturama)
 * @param cadenaQr          contenido del código QR de la representación impresa
 * @param mensajeError      detalle del error cuando el estado es de fallo; null si todo OK
 */
public record Resultado(
        EstadoComprobante estado,
        String identificadorFiscal,
        String xmlBase64,
        String pdfBase64,
        String sello,
        String cadenaQr,
        String mensajeError
) {

    /** Atajo para un resultado de error de comunicación con el PAC. */
    public static Resultado errorComunicacion(String mensaje) {
        return new Resultado(EstadoComprobante.ERROR_COMUNICACION, null, null, null, null, null, mensaje);
    }
}
