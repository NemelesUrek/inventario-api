package com.nemeles.inventario.einvoicing.mx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nemeles.inventario.einvoicing.api.Comprobante;
import com.nemeles.inventario.einvoicing.api.EstadoComprobante;
import com.nemeles.inventario.einvoicing.api.FacturacionElectronicaProvider;
import com.nemeles.inventario.einvoicing.api.Pais;
import com.nemeles.inventario.einvoicing.api.Resultado;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Conector de facturación electrónica para MÉXICO (CFDI 4.0) vía el PAC Facturama.
 *
 * <p>Habla con la API web de Facturama usando autenticación básica (Basic Auth) y
 * únicamente la {@link java.net.http.HttpClient} de la JDK + Jackson (sin
 * dependencias nuevas). Endpoints usados (base sandbox por defecto):</p>
 * <ul>
 *   <li>Emitir CFDI 4.0: {@code POST /3/cfdis}</li>
 *   <li>Descargar XML:   {@code GET /cfdi/xml/issued/{id}}  (devuelve Content en Base64)</li>
 *   <li>Descargar PDF:   {@code GET /cfdi/pdf/issued/{id}}  (devuelve Content en Base64)</li>
 *   <li>Consultar:       {@code GET /cfdi/{id}}</li>
 *   <li>Cancelar:        {@code DELETE /cfdi/{id}?type=issued&motive=...}</li>
 * </ul>
 *
 * <p>Las credenciales se leen de configuración ({@code facturama.user} /
 * {@code facturama.password}), que a su vez mapean las variables de entorno
 * {@code FACTURAMA_USER} / {@code FACTURAMA_PASSWORD} en application.properties.
 * NO se guardan credenciales en el código.</p>
 */
@Component
public class FacturamaMxProvider implements FacturacionElectronicaProvider {

    private static final Logger log = LoggerFactory.getLogger(FacturamaMxProvider.class);

    /** Moneda por defecto si el comprobante no la trae. */
    private static final String MONEDA_DEFECTO = "MXN";

    private final String baseUrl;
    private final String usuario;
    private final String password;
    private final ObjectMapper mapper;
    private final HttpClient http;

    public FacturamaMxProvider(
            @Value("${facturama.base-url:https://apisandbox.facturama.mx}") String baseUrl,
            @Value("${facturama.user:}") String usuario,
            @Value("${facturama.password:}") String password,
            ObjectMapper mapper) {
        // Normaliza la base para poder concatenar rutas sin barras duplicadas.
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.usuario = usuario;
        this.password = password;
        this.mapper = mapper;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public Pais pais() {
        return Pais.MX;
    }

    @Override
    public Resultado emitir(Comprobante c) {
        try {
            String cuerpo = mapper.writeValueAsString(aJsonFacturama(c));
            HttpRequest req = peticionBase("/3/cfdis")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (!esExitoso(res.statusCode())) {
                log.warn("Facturama rechazó la emisión (HTTP {}): {}", res.statusCode(), res.body());
                return new Resultado(EstadoComprobante.RECHAZADO, null, null, null, null, null,
                        "Facturama HTTP " + res.statusCode() + ": " + res.body());
            }

            JsonNode cfdi = mapper.readTree(res.body());
            String id = texto(cfdi, "Id");
            JsonNode timbre = cfdi.path("Complement").path("TaxStamp");
            String uuid = timbre.path("Uuid").asText(null);
            String sello = timbre.path("CfdiSign").asText(null);

            // El XML y el PDF se descargan por endpoints aparte; si fallan, no
            // invalidamos el timbre: devolvemos ACEPTADO con el UUID igualmente.
            String xmlBase64 = descargarContenido("/cfdi/xml/issued/" + id);
            String pdfBase64 = descargarContenido("/cfdi/pdf/issued/" + id);

            return new Resultado(EstadoComprobante.ACEPTADO, uuid, xmlBase64, pdfBase64, sello, null, null);
        } catch (Exception e) {
            // Cualquier fallo de red/serialización se reporta como error de comunicación.
            log.error("Error comunicándose con Facturama al emitir", e);
            return Resultado.errorComunicacion(mensaje(e));
        }
    }

    @Override
    public Resultado cancelar(String id, String motivo) {
        try {
            // type=issued: cancelamos un CFDI emitido por nosotros. motive: catálogo SAT.
            String ruta = "/cfdi/" + url(id) + "?type=issued&motive=" + url(motivo);
            HttpRequest req = peticionBase(ruta).DELETE().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (!esExitoso(res.statusCode())) {
                log.warn("Facturama rechazó la cancelación (HTTP {}): {}", res.statusCode(), res.body());
                return new Resultado(EstadoComprobante.RECHAZADO, id, null, null, null, null,
                        "Facturama HTTP " + res.statusCode() + ": " + res.body());
            }
            return new Resultado(EstadoComprobante.CANCELADO, id, null, null, null, null, null);
        } catch (Exception e) {
            log.error("Error comunicándose con Facturama al cancelar", e);
            return Resultado.errorComunicacion(mensaje(e));
        }
    }

    @Override
    public EstadoComprobante consultarEstado(String id) {
        try {
            HttpRequest req = peticionBase("/cfdi/" + url(id)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 404) {
                return EstadoComprobante.RECHAZADO;
            }
            if (!esExitoso(res.statusCode())) {
                return EstadoComprobante.ERROR_COMUNICACION;
            }
            JsonNode cfdi = mapper.readTree(res.body());
            // Facturama expone "Status" (p. ej. "active" / "canceled"). Lo mapeamos a nuestro enum.
            String estado = cfdi.path("Status").asText("").toLowerCase();
            return switch (estado) {
                case "canceled", "cancelled" -> EstadoComprobante.CANCELADO;
                case "active" -> EstadoComprobante.ACEPTADO;
                case "" -> EstadoComprobante.PENDIENTE;
                default -> EstadoComprobante.ACEPTADO;
            };
        } catch (Exception e) {
            log.error("Error comunicándose con Facturama al consultar estado", e);
            return EstadoComprobante.ERROR_COMUNICACION;
        }
    }

    // ---------------------------------------------------------------------
    // Mapeo Comprobante (neutro) -> JSON "Cfdi" de Facturama
    // ---------------------------------------------------------------------

    /**
     * Traduce el {@link Comprobante} neutro al JSON que espera Facturama para un CFDI 4.0.
     *
     * <p>TODO: para producción conviene apoyarse en los catálogos completos del SAT
     * (c_ClaveProdServ, c_ClaveUnidad, c_RegimenFiscal, c_UsoCFDI, c_FormaPago,
     * c_ObjetoImp) y validar/normalizar cada clave antes de enviar. Aquí se pasan
     * tal cual vienen del comprobante.</p>
     */
    private ObjectNode aJsonFacturama(Comprobante c) {
        ObjectNode raiz = mapper.createObjectNode();

        // Tipo de comprobante: "I" (ingreso) por defecto si no se especifica.
        raiz.put("CfdiType", c.tipo() == null || c.tipo().isBlank() ? "I" : c.tipo());
        raiz.put("Currency", c.moneda() == null || c.moneda().isBlank() ? MONEDA_DEFECTO : c.moneda());
        if (c.formaPago() != null) {
            raiz.put("PaymentForm", c.formaPago());
        }
        if (c.metodoPago() != null) {
            raiz.put("PaymentMethod", c.metodoPago());
        }
        // Exportacion "01" = No aplica (caso más común en venta nacional). TODO: parametrizar.
        raiz.put("Exportation", "01");

        // Lugar de expedición (CP del emisor). Obligatorio en CFDI 4.0.
        if (c.emisor() != null && c.emisor().lugarExpedicion() != null) {
            raiz.put("ExpeditionPlace", c.emisor().lugarExpedicion());
        }

        // Receptor.
        Comprobante.Receptor r = c.receptor();
        if (r != null) {
            ObjectNode receptor = raiz.putObject("Receiver");
            receptor.put("Rfc", r.identificadorFiscal());
            receptor.put("Name", r.nombre());
            receptor.put("CfdiUse", r.usoCfdi());
            receptor.put("FiscalRegime", r.regimenFiscal());
            receptor.put("TaxZipCode", r.codigoPostal());
        }

        // Conceptos / partidas.
        ArrayNode items = raiz.putArray("Items");
        if (c.lineas() != null) {
            for (Comprobante.LineaComprobante l : c.lineas()) {
                items.add(aItemFacturama(l));
            }
        }
        return raiz;
    }

    /** Mapea una línea neutra a un Item de Facturama (con su IVA al 16% por defecto). */
    private ObjectNode aItemFacturama(Comprobante.LineaComprobante l) {
        ObjectNode item = mapper.createObjectNode();
        item.put("Quantity", str(l.cantidad()));
        item.put("ProductCode", l.claveProdServ()); // c_ClaveProdServ del SAT
        item.put("UnitCode", l.claveUnidad());      // c_ClaveUnidad del SAT
        if (l.unidad() != null) {
            item.put("Unit", l.unidad());
        }
        item.put("Description", l.descripcion());
        item.put("UnitPrice", str(l.valorUnitario()));
        BigDecimal importe = l.importe() != null
                ? l.importe()
                : seguro(l.cantidad()).multiply(seguro(l.valorUnitario()));
        item.put("Subtotal", str(importe));

        // ObjetoImp "02" = Sí objeto de impuesto. TODO: derivar del producto si hay exentos.
        item.put("TaxObject", "02");

        // IVA trasladado al 16% (caso general en México).
        // TODO: soportar tasas distintas, retenciones (ISR/IVA RET) y exentos según el catálogo.
        BigDecimal tasaIva = new BigDecimal("0.16");
        BigDecimal iva = importe.multiply(tasaIva);
        ArrayNode taxes = item.putArray("Taxes");
        ObjectNode ivaNode = taxes.addObject();
        ivaNode.put("Name", "IVA");
        ivaNode.put("Rate", str(tasaIva));
        ivaNode.put("Total", str(iva));
        ivaNode.put("Base", str(importe));
        ivaNode.put("IsRetention", "false");
        ivaNode.put("IsFederalTax", "true");

        item.put("Total", str(importe.add(iva)));
        return item;
    }

    // ---------------------------------------------------------------------
    // Utilidades HTTP / JSON
    // ---------------------------------------------------------------------

    /** Construye una petición con la URL base, el header de autenticación básica y JSON. */
    private HttpRequest.Builder peticionBase(String ruta) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + ruta))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Basic " + autenticacionBasica())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    /** Token Base64 de "usuario:password" para el header Authorization. */
    private String autenticacionBasica() {
        String credenciales = usuario + ":" + password;
        return Base64.getEncoder().encodeToString(credenciales.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Descarga un recurso cuyo cuerpo es un JSON {@code { "Content": "<base64>", ... }}
     * (así devuelve Facturama el XML y el PDF) y regresa el campo {@code Content}.
     * Si falla, devuelve null (no invalida el timbre ya obtenido).
     */
    private String descargarContenido(String ruta) {
        try {
            HttpRequest req = peticionBase(ruta).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (!esExitoso(res.statusCode())) {
                log.warn("No se pudo descargar {} (HTTP {})", ruta, res.statusCode());
                return null;
            }
            return mapper.readTree(res.body()).path("Content").asText(null);
        } catch (Exception e) {
            log.warn("Error descargando {}: {}", ruta, mensaje(e));
            return null;
        }
    }

    private static boolean esExitoso(int codigo) {
        return codigo >= 200 && codigo < 300;
    }

    private static String texto(JsonNode nodo, String campo) {
        return nodo.path(campo).asText(null);
    }

    /** Codifica un segmento para usarlo en la URL (id o motivo). */
    private static String url(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }

    /** BigDecimal a texto plano (Facturama acepta los importes como cadenas). */
    private static String str(BigDecimal v) {
        return seguro(v).toPlainString();
    }

    private static BigDecimal seguro(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String mensaje(Exception e) {
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }
}
