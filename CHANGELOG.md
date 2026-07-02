# Changelog — Stockly (inventario-api)

Historial de iteraciones del proyecto. Cada entrada corresponde a una tanda de
cambios verificada (pruebas automatizadas + verificación funcional de la UI).

## [Iteración 12] — 2026-06-23
### Rendimiento y despliegue
- Optimización del arranque en frío en Render (free tier): `spring.main.lazy-initialization=true`,
  `spring.jmx.enabled=false` y `springdoc.packages-to-scan` acotado para sacar el escaneo
  eager de springdoc del camino crítico del boot.
- Consola H2 deshabilitada en el perfil desplegado (endurecimiento de seguridad).
- `Dockerfile` con flags de JVM afinadas para contenedores de 512 MB:
  `-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=65.0 -Xss512k -XX:+ExitOnOutOfMemoryError`.
- Verificado empíricamente bajo `-XX:MaxRAM=512m`: arranque correcto, health check en 0.15 s,
  seed de datos y Swagger operativos, suite de 10 pruebas en verde.

## [Iteración 11] — 2026-06-22
### Módulo de categorías (end-to-end)
- **Backend:** campo `categoria` en la entidad `Producto`, en los DTO de creación/actualización/
  respuesta, en `ProductoService` y en el seed de datos (5 categorías). Pruebas ampliadas.
- **Frontend:** campo de categoría en el modal (con `datalist` de categorías existentes),
  etiqueta de categoría en la tabla, filtro por categoría, gráfica de valor por categoría
  en Reportes e inclusión de la categoría en el export CSV.

## [Iteración 10] — 2026-06-22
### Ayuda y descubribilidad
- Overlay de atajos de teclado (tecla `?` o desde la paleta de comandos), con `role=dialog`
  y cierre con Esc.
- `document.title` dinámico por vista.
- Aviso de la paleta de comandos (Ctrl+K) en la primera visita.

## [Iteración 9] — 2026-06-22
### Tabla de inventario avanzada
- Orden por columnas (SKU/Producto/Precio/Stock) con clic en el encabezado, ascendente/
  descendente e indicador visual.
- Chip "Solo bajo stock" para filtrar la tabla.
- Estados vacíos con contexto (búsqueda sin resultados, sin bajo stock, sin productos).

## [Iteración 8] — 2026-06-22
### Reset de datos de demo
- **Backend:** `DemoDataService` con siembra reutilizable (`seedIfEmpty` + `reset`) y nuevo
  endpoint **POST /api/demo/reset** que restaura el estado inicial. Suite en 10 pruebas verdes.
- **Frontend:** acción "Reiniciar datos de demo" disponible en Configuración, en la paleta de
  comandos y en la vista API, con diálogo de confirmación propio.

## [Iteración 7] — 2026-06-22
### Pulido de experiencia de usuario
- Skeletons de carga en la tabla mientras responde la API.
- Toasts tipificados (éxito/error/info) aplicados a todas las acciones.
- Focus-trap (Tab) en modales y en la paleta de comandos.

## [Iteración 6] — 2026-06-22
### Tarjeta social y documentación
- Imagen Open Graph 1200×630 (`/og.png`) generada con Pillow (`tools/make_og.py`), con metas
  `og:image` y `twitter:card=summary_large_image` apuntando a la URL del demo.
- README actualizado: demo en vivo como enlace principal, sección de la app y filas nuevas de
  endpoints (`/api/movimientos`, `/api/stats`).

## [Iteración 5] — 2026-06-22
### Configuración y accesibilidad
- Vista Configuración: tema (claro/oscuro), densidad (cómoda/compacta) y animaciones (sí/no),
  todo persistido en `localStorage`. Sección "Acerca de" con stack y enlaces.
- Favicon SVG + metas OG/Twitter/theme-color (el theme-color acompaña al tema activo).
- Accesibilidad: `aria-label` en botones de icono; `role=dialog`/`alertdialog`, `aria-modal`
  y `aria-labelledby` en modales y paleta.

## [Iteración 4] — 2026-06-22
### Estadísticas server-side
- **Backend:** nuevo endpoint **GET /api/stats** (`StatsController` + `StatsResponse` + servicio
  y consulta `countByTipo`): totales de productos, unidades, valor formateado, bajo stock y
  conteo de movimientos por tipo. Prueba adicional (9 en verde).
- **Reportes:** franja de 7 métricas calculadas en el servidor + gráfico de actividad
  (entradas vs. salidas).

## [Iteración 3] — 2026-06-22
### Paleta de comandos y atajos
- Command palette (Ctrl/Cmd + K) con búsqueda difusa sobre navegación, acciones (nuevo producto,
  CSV, tema, actualizar) y productos; navegación por teclado y cierre con Esc.
- Atajos globales: Ctrl+K (paleta), `/` (buscar), `n` (nuevo), `t` (tema), Esc (cerrar),
  con guardas para no dispararse al escribir en formularios.
- Diálogo de confirmación propio en reemplazo del `confirm()` nativo.

## [Iteración 2] — 2026-06-22
### CRUD completo y auditoría global
- **Frontend:** vista Movimientos con feed de auditoría global, filtros (todos/entradas/salidas),
  badges de tipo, stock final, motivo y tiempo relativo. Edición y eliminación de productos desde
  la tabla (modal reutilizado vía PUT; confirmación para DELETE).
- **Backend:** nuevo endpoint **GET /api/movimientos** (`MovimientoController` + repositorio +
  servicio, respetando la separación por capas).
- Pruebas: +2 (feed global y actualizar/eliminar). 8 en verde.

## [Iteración 1] — 2026-06-22
### Aplicación multi-vista "Stockly"
- Reestructura de landing de una sola página a aplicación con sidebar y 5 vistas (Panel,
  Inventario, Reportes, API & Docs, Contratar) con router por hash en JavaScript puro.
- Marca "Stockly" con logo, header por vista y badge de estado de la API.
- Tema claro/oscuro persistido en `localStorage`.
- Vista Reportes con gráficas SVG propias (sin librerías): valor por producto, salud del stock
  y distribución de unidades.
- Panel con KPIs, alertas y probador de la API (entrada/salida real con manejo de 409).
- Inventario con buscador, semáforo de stock, paginación, historial por producto y export CSV.
- Diseño responsive (sidebar colapsable en móvil), focus rings y micro-animaciones.

---

## Backlog (próximas mejoras)
- QA de humo sobre el deploy en vivo (11 endpoints + app).
- Paginación server-side cuando crezca el catálogo.
- Pruebas e2e del frontend.
- Más reportes y gráficas (tendencia, rotación) y filtros adicionales.
