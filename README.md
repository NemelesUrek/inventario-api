# 📦 API de Control de Inventario

[![CI](https://github.com/NemelesUrek/inventario-api/actions/workflows/maven.yml/badge.svg)](https://github.com/NemelesUrek/inventario-api/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)

API REST para gestionar productos y stock, con **auditoría de cada movimiento** y **alertas de reposición**. Backend en **Java + Spring Boot**, probado y desplegable.

> **▶ Demo en vivo:** **https://inventario-api-h6b3.onrender.com/swagger-ui/index.html**
> *(pruébala en el navegador — sin instalar nada — con el botón **Try it out**. Free tier: la primera petición tras inactividad puede tardar ~50 s en despertar.)*

---

## El problema
En una tienda o almacén, el stock se descuadra fácil: dos ventas a la vez, un ajuste mal hecho, o nadie sabe *cuándo reponer*. Y si el dinero se maneja con decimales, aparecen errores de centavos.

## La solución
- **API REST limpia**: CRUD de productos + entradas/salidas de stock, con búsqueda, paginación y códigos HTTP correctos (`201`, `404`, `409`).
- **Operaciones transaccionales**: cada cambio de stock se guarda junto a su registro de auditoría dentro de una transacción (o ambos, o ninguno).
- **Auditoría append-only**: todo movimiento queda registrado (tipo, cantidad, stock resultante, motivo, fecha).
- **Alertas de reposición**: endpoint que lista los productos en o por debajo de su mínimo.
- **Dinero en centavos** (`long`), nunca en coma flotante → sin errores de redondeo.
- **Errores consistentes** en formato RFC-7807 (`ProblemDetail`).

---

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/productos?buscar=&page=&size=` | Listar (búsqueda + paginación) |
| `GET` | `/api/productos/{id}` | Obtener un producto |
| `POST` | `/api/productos` | Crear (201 + `Location`; 409 si SKU duplicado) |
| `PUT` | `/api/productos/{id}` | Actualizar datos |
| `DELETE` | `/api/productos/{id}` | Eliminar (204) |
| `POST` | `/api/productos/{id}/entrada` | Entrada de stock (+ auditoría) |
| `POST` | `/api/productos/{id}/salida` | Salida de stock (**409** si no alcanza) |
| `GET` | `/api/productos/{id}/movimientos` | Historial de movimientos |
| `GET` | `/api/productos/bajo-stock` | Alertas de reposición |

Documentación interactiva en **`/swagger-ui.html`** · spec OpenAPI en **`/v3/api-docs`**.

## Pruébala (curl)
```bash
BASE=http://localhost:8080

# Crear
curl -s -X POST $BASE/api/productos -H "Content-Type: application/json" \
  -d '{"sku":"LAP-900","nombre":"Laptop 14\"","precioCentavos":1299900,"stockInicial":5,"stockMinimo":2}'

# Salida que excede el stock -> 409
curl -s -X POST $BASE/api/productos/1/salida -H "Content-Type: application/json" \
  -d '{"cantidad":999,"motivo":"Venta"}'

# Alertas de stock bajo
curl -s $BASE/api/productos/bajo-stock
```

---

## Correr en local
Requiere **JDK 17+**. La base de datos es **H2 en memoria** (no instalas nada).
```bash
./mvnw spring-boot:run
# luego abre http://localhost:8080
```

## Pruebas
```bash
./mvnw test
```
Suite con JUnit 5 + MockMvc (creación 201, 404, validación 400, **409 por stock insuficiente**). Validadas en **CI (GitHub Actions)** en cada push.

## Stack
Java 17 · Spring Boot 3.5 (Web, Data JPA, Validation) · H2 / HikariCP · springdoc-openapi (Swagger UI) · Maven · JUnit 5 · Docker.

## Deploy (Render, gratis)
1. Sube este repo a GitHub.
2. En [render.com](https://render.com) → **New → Web Service** → conecta el repo → Render detecta el `Dockerfile` solo (o usa **Blueprint** con `render.yaml`).
3. Plan **Free**. Render asigna el puerto vía `$PORT` (ya configurado). Health check: `/actuator/health`.
4. Copia la URL pública al README y a tus propuestas.

---

**Autor:** Nemeles — Backend Engineer (Java · Spring Boot · REST APIs · SQL). GitHub: **NemelesUrek**.
