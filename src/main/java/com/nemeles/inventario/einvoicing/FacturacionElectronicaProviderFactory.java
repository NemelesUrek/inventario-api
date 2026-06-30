package com.nemeles.inventario.einvoicing;

import com.nemeles.inventario.einvoicing.api.FacturacionElectronicaProvider;
import com.nemeles.inventario.einvoicing.api.Pais;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Resuelve el {@link FacturacionElectronicaProvider} que corresponde a cada país.
 *
 * <p>Spring inyecta TODOS los beans que implementen la interfaz; aquí se indexan
 * por su {@link Pais}. Para añadir un país nuevo basta con registrar un nuevo
 * {@code @Component} que implemente la interfaz: la factory lo recoge solo.</p>
 */
@Service
public class FacturacionElectronicaProviderFactory {

    private final Map<Pais, FacturacionElectronicaProvider> proveedores = new EnumMap<>(Pais.class);

    public FacturacionElectronicaProviderFactory(List<FacturacionElectronicaProvider> beans) {
        for (FacturacionElectronicaProvider p : beans) {
            proveedores.put(p.pais(), p);
        }
    }

    /**
     * Devuelve el proveedor del país pedido.
     *
     * @throws IllegalArgumentException si no hay un conector registrado para ese país.
     */
    public FacturacionElectronicaProvider of(Pais pais) {
        FacturacionElectronicaProvider p = proveedores.get(pais);
        if (p == null) {
            throw new IllegalArgumentException(
                    "No hay un proveedor de facturación electrónica registrado para el país " + pais);
        }
        return p;
    }

    /** Indica si hay soporte para el país (útil para validaciones tempranas). */
    public boolean soporta(Pais pais) {
        return proveedores.containsKey(pais);
    }
}
