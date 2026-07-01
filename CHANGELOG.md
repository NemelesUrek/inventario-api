# Changelog — Stockly (Web)

Registro de cambios de la app web de inventario. Fechas en formato AAAA-MM-DD.

## [No publicado]

### Agregado
- **Facturación CFDI 4.0 (México)** — nueva vista `#view-facturacion` integrada a la web:
  - Datos del **emisor** (RFC, razón social, régimen fiscal SAT, CP) con guardado en el navegador.
  - Datos del **receptor** (RFC, nombre, CP fiscal, régimen, uso CFDI) con catálogos SAT.
  - **Conceptos**: se agregan desde el inventario (selector de productos) o de forma manual; tabla editable con cantidad, valor unitario e importe calculado en vivo.
  - **Totales** con IVA configurable (16 % / 8 % / 0 %): subtotal, impuestos y total.
  - **Emisión**: valida RFC/CP/conceptos y hace `POST /api/facturas` al conector PAC (Facturama).
    Si no hay backend conectado (modo demo / GitHub Pages) genera un **timbre simulado** claramente etiquetado como "Simulación".
  - **Resultado**: estado de timbrado, folio fiscal (UUID) con copiar, y descarga de XML/PDF cuando el PAC los devuelve.
  - **Historial** de facturas emitidas (persistido en el navegador).
  - Permiso `factura.emitir` (roles ADMINISTRADOR y VENTAS); comando de paleta "Ir a Facturación";
    etiqueta "Facturación CFDI 4.0 (México)" marcada como **listo** en la sección Contratar.

### Pendiente / próximos gaps detectados en la web
- **Clientes (receptores guardados)** — catálogo de clientes para elegir el receptor en Facturación
  sin re-teclear el RFC, y ligarlo al historial de facturas. (Alta prioridad: integra con lo recién hecho.)
- **Categorías** — CRUD de categorías de producto desde la UI (hoy se editan por campo libre).
- **Punto de venta (POS)** ligero — generar venta + descuento de stock + factura en un flujo.
- **Reabasto / órdenes de compra** a partir de alertas de stock bajo.
- **Internacionalización** del comprobante (el backend ya es multi-país; falta exponer otros países en la UI).
