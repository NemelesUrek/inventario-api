package com.nemeles.inventario.einvoicing.api;

/**
 * Contrato de un conector de facturación electrónica para un país concreto.
 *
 * <p>Cada implementación habla con su PAC/autoridad (en México, Facturama) pero
 * expone siempre la misma interfaz neutra, de modo que el resto de la app no
 * tenga que saber de qué país se trata.</p>
 */
public interface FacturacionElectronicaProvider {

    /** País que atiende esta implementación. */
    Pais pais();

    /**
     * Emite (timbra) un comprobante.
     *
     * @param c comprobante neutro a emitir
     * @return resultado con UUID/sello/XML/PDF si todo salió bien, o un
     *         {@link Resultado} con {@link EstadoComprobante#ERROR_COMUNICACION}
     *         si falló la comunicación con el PAC.
     */
    Resultado emitir(Comprobante c);

    /**
     * Cancela un comprobante ya emitido.
     *
     * @param id     identificador del comprobante en el PAC (en Facturama, el Id interno)
     * @param motivo motivo de cancelación (en México: catálogo de motivos del SAT, p. ej. "02")
     */
    Resultado cancelar(String id, String motivo);

    /**
     * Consulta el estado actual de un comprobante en el PAC/autoridad.
     *
     * @param id identificador del comprobante en el PAC
     */
    EstadoComprobante consultarEstado(String id);
}
