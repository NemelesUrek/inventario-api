# Bitácora de mejoras — Stockly (loop de pulido)

Objetivo: que la demo deje de parecer "mi primera web con tutorial de 10 min" y
se vea como una **aplicación interactiva completa y profesional de nivel senior**.

## Iteración 1 — App shell multi-vista
- Reestructurado de landing de una sola página a **aplicación con sidebar + 5 vistas**
  (Panel, Inventario, Reportes, API & Docs, Contratar) con router por hash (vanilla JS).
- **Marca "Stockly"** + logo, header por vista, badge de estado de API.
- **Tema claro/oscuro** con toggle (persistido en localStorage).
- **Vista Reportes** nueva con **gráficas SVG** propias (sin librerías): valor por
  producto (barras), salud del stock (donut), distribución de unidades.
- **Modal "Nuevo producto"** (alta real vía POST).
- Panel: KPIs + alertas + "probar la API" (entrada/salida real, manejo de 409).
- Inventario: buscador, tabla con semáforo, paginación, historial por producto, CSV.
- Responsive (sidebar colapsable en móvil), accesible (focus rings), micro-animaciones.

## Iteración 2 — CRUD completo + auditoría global
- **Vista Movimientos** nueva: feed de auditoría global con filtros (Todos/Entradas/Salidas),
  badges de tipo, cantidad con color, stock final, motivo y "hace X" relativo.
- **Backend:** nuevo endpoint **GET /api/movimientos** (MovimientoController + repo
  `findTop100ByOrderByFechaDescIdDesc` + service, respetando capas).
- **Editar / eliminar** productos desde la tabla (modal reutilizado en modo edición vía
  PUT; botón eliminar con confirmación vía DELETE). Columna "Acciones".
- **Tests:** +2 (feed global de movimientos, y actualizar+eliminar). **8 tests verdes.**
- Verificado local por DOM: 6 nav, MOVS=6, 12 botones de acción, modal de edición OK.

## Iteración 3 — Paleta de comandos + atajos + UX pro
- **Command palette (⌘K / Ctrl+K)**: overlay con búsqueda difusa sobre navegación,
  acciones (nuevo, CSV, tema, actualizar) y productos (editar). Navegación con
  flechas + Enter, ratón con hover, cierre con Esc. Botón "Buscar ⌘K" en el topbar.
- **Atajos globales**: Ctrl+K (paleta), `/` (buscar inventario), `n` (nuevo),
  `t` (tema), Esc (cerrar). Guardados para no dispararse al escribir.
- **Diálogo de confirmación propio** (reemplaza `confirm()` nativo) para eliminar.
- Verificado por DOM: 16 items, filtros, nav por teclado, Ctrl+K, confirm OK.

## Iteración 4 — Estadísticas server-side + reportes ricos
- **Backend:** nuevo endpoint **GET /api/stats** (StatsController + StatsResponse +
  service `estadisticas()` + repo `countByTipo`): totales de productos, unidades,
  valor (formateado), bajo-stock y conteo de movimientos por tipo.
- **Reportes:** franja de 7 métricas server-side + gráfico de **actividad**
  (entradas vs. salidas). Gráficas previas intactas.
- **Tests:** +1 (resumen agregado). **9 tests verdes.**
- Verificado por DOM: 7 statpills, valor $43,355.00, actividad 6/0.

## Iteración 5 — Configuración + favicon/OG + accesibilidad
- **Vista Configuración**: tema (claro/oscuro), **densidad** (cómoda/compacta) y
  **animaciones** (sí/no), todo persistido en localStorage y aplicado en init.
  Sección "Acerca de" con stack + enlaces (GitHub, Swagger).
- **Favicon SVG** (cubo de marca) + **meta OG/Twitter/theme-color** (preview pro
  al compartir el enlace; theme-color cambia con el tema).
- **Accesibilidad**: aria-labels en botones de icono; role=dialog/alertdialog +
  aria-modal/labelledby en modales y paleta.
- Verificado por DOM: 7 nav, densidad compacta OK, tema sincroniza seg + meta, roles OK.

## Iteración 6 — Tarjeta social (OG) + README
- **Imagen OG 1200×630** (`/og.png`) generada con Pillow (`tools/make_og.py`):
  marca, tagline, 3 chips ($43,355 / 8 endpoints / 9 tests·CI) y autor. Meta
  `og:image` + `twitter:card=summary_large_image` con URL absoluta del demo.
- **README actualizado**: app en vivo como enlace principal, sección "La app
  (interfaz)", y filas nuevas de endpoints (`/api/movimientos`, `/api/stats`).
- Verificado: og.png se sirve (200, image/png, 96KB) y la meta está presente.

## Iteración 7 — Pulido UX senior
- **Skeletons de carga** en la tabla mientras llega la API (8 filas shimmer).
- **Toasts con tipo** (éxito/error/info) con acento de color; aplicados a todas
  las acciones (crear/editar/eliminar/stock/CSV/actualizar/rechazos).
- **Focus-trap** (Tab) en modales y paleta de comandos.
- Verificado por DOM: skeleton 8×6, toast "ok show", trap activo, sin regresiones.

## Iteración 8 — Reset de demo
- **Backend:** `DemoDataService` (siembra reutilizable: `seedIfEmpty` + `reset`),
  `DataSeeder` ahora delega; nuevo **POST /api/demo/reset** (DemoController) que
  borra todo y vuelve a sembrar. **+1 test → 10 verdes.**
- **Frontend:** botón "Reiniciar datos de demo" en Configuración + comando en la
  paleta + endpoint en la vista API. Confirmación con diálogo propio.
- Verificado: 7→reset→6 vía curl; botón/comando/endpoint presentes.

## Iteración 9 — Orden, filtro y estados vacíos
- **Orden por columnas** (SKU/Producto/Precio/Stock) con clic en el encabezado,
  asc/desc y flecha indicadora.
- **Chip "Solo bajo stock"** que filtra la tabla.
- **Estados vacíos con contexto** (búsqueda sin resultados / sin bajo stock / sin
  productos) con icono.
- Verificado por DOM: precio asc $59.90 / desc $649.00, filtro 2 filas, mensajes OK.

## Iteración 10 — Ayuda, título y descubribilidad
- **Overlay de atajos** (tecla `?` o comando en paleta): lista de 6 atajos,
  role=dialog, cierra con Esc.
- **document.title por vista** ("Movimientos · Stockly — Nemeles", etc.).
- **Hint de ⌘K en primera visita** (toast informativo, una sola vez).
- Verificado por DOM: títulos OK, ayuda abre/cierra por ? y Esc, comando en paleta.

## Estado: app completa de nivel senior
11 endpoints (4 controllers) · 10 tests + CI · 7 vistas SPA · CRUD + auditoría +
reportes server-side + paleta ⌘K + configuración + reset de demo + a11y + OG.

## Iteración 11 — Módulo de Categorías (multiuso)
- **Backend end-to-end:** campo `categoria` en `Producto` (+constructor/getter/setter),
  en `CrearProductoRequest`/`ActualizarProductoRequest`/`ProductoResponse`, en
  `ProductoService.crear/actualizar` y en el seed (5 categorías). Test ampliado.
- **Frontend:** campo en el modal (con `datalist` de categorías existentes), **etiqueta
  de categoría** en la tabla, **filtro por categoría** (select), **gráfica "valor por
  categoría"** en Reportes y categoría en el CSV.
- Verificado por DOM: filtro Hogar→2, 5 barras de categoría, modal precarga, 10 tests.

## Pendiente / próximas iteraciones (nice-to-have)
- [ ] QA final sobre el deploy en vivo (humo de los 11 endpoints + app).
- [ ] Paginación server-side real cuando crezca el catálogo.
- [ ] Tests e2e del frontend.
- [ ] Más reportes/gráficas (tendencia, rotación) y filtros.
- [ ] Command palette / atajos de teclado.
- [ ] Estados vacíos/skeleton más pulidos, toasts apilables.
- [ ] Paginación server-side real cuando crezca el catálogo.
- [ ] Tests del frontend / smoke e2e.
