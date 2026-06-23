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

## Pendiente / próximas iteraciones
- [ ] Editar y eliminar productos desde la UI (requiere PUT/DELETE en backend).
- [ ] Vista global de Movimientos (auditoría) + endpoint /api/movimientos.
- [ ] Endpoint /api/stats para los KPIs/reportes server-side.
- [ ] Más reportes/gráficas (tendencia, rotación) y filtros.
- [ ] Command palette / atajos de teclado.
- [ ] Estados vacíos/skeleton más pulidos, toasts apilables.
- [ ] Paginación server-side real cuando crezca el catálogo.
- [ ] Tests del frontend / smoke e2e.
