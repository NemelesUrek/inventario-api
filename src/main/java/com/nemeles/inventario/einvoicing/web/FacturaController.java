package com.nemeles.inventario.einvoicing.web;

import com.nemeles.inventario.einvoicing.FacturacionElectronicaProviderFactory;
import com.nemeles.inventario.einvoicing.api.Comprobante;
import com.nemeles.inventario.einvoicing.api.Pais;
import com.nemeles.inventario.einvoicing.api.Resultado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de facturación electrónica. Recibe un {@link Comprobante} neutro y, según
 * su {@link Pais}, delega en el proveedor correspondiente (en México: CFDI 4.0 vía Facturama).
 *
 * <p>Seguridad: el proyecto usa seguridad por URL ({@code /api/**} exige sesión
 * iniciada en {@code SecurityConfig}), por lo que este endpoint ya queda protegido.
 * TODO: si se habilita method security ({@code @EnableMethodSecurity}), añadir
 * {@code @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE','CAJERO')")} para
 * restringir la emisión a esos roles.</p>
 */
@Tag(name = "Facturación electrónica",
        description = "Emite comprobantes fiscales neutros por país (México: CFDI 4.0 vía Facturama)")
@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    private final FacturacionElectronicaProviderFactory factory;

    public FacturaController(FacturacionElectronicaProviderFactory factory) {
        this.factory = factory;
    }

    /**
     * Emite un comprobante. El país se toma del propio cuerpo del comprobante.
     *
     * @return 200 con el {@link Resultado} (incluido el caso de error de
     *         comunicación, para que el cliente vea el estado y el mensaje).
     */
    @PostMapping
    @Operation(summary = "Emitir comprobante",
            description = "Recibe un Comprobante neutro y lo emite con el proveedor del país indicado.")
    public ResponseEntity<Resultado> emitir(@Valid @RequestBody EmitirFacturaRequest req) {
        Resultado resultado = factory.of(req.pais()).emitir(req.comprobante());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Cuerpo de la petición de emisión. Se usa un DTO sencillo para validar el país
     * y dejar claro que el comprobante es obligatorio.
     *
     * @param pais        país fiscal con el que emitir (debe coincidir con el del comprobante)
     * @param comprobante comprobante neutro a timbrar
     */
    public record EmitirFacturaRequest(
            @NotNull Pais pais,
            @NotNull @Valid Comprobante comprobante
    ) {
    }
}
