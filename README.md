🇬🇧 English | [🇪🇸 Español](README.es.md)

# 📦 Inventory Control API

[![CI](https://github.com/NemelesUrek/inventario-api/actions/workflows/maven.yml/badge.svg)](https://github.com/NemelesUrek/inventario-api/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)

REST API to manage products and stock, with **an audit trail for every movement** and **restock alerts**. Backend in **Java + Spring Boot**, tested and deployable.

> **▶ Live app:** **https://inventario-api-h6b3.onrender.com** *(free tier — first load takes ~50 s)*
> **▶ Interactive API (Swagger):** **https://inventario-api-h6b3.onrender.com/swagger-ui/index.html**
> *(try them right in the browser, nothing to install)*

---

## The problem
In a store or warehouse, stock counts drift easily: two sales at the same time, a bad adjustment, or nobody knows *when to restock*. And if money is handled with floating-point decimals, cent-level errors creep in.

## The solution
- **Clean REST API**: product CRUD + stock in/out operations, with search, pagination and correct HTTP status codes (`201`, `404`, `409`).
- **Transactional operations**: every stock change is saved together with its audit record inside a single transaction (both or neither).
- **Append-only audit trail**: every movement is recorded (type, quantity, resulting stock, reason, timestamp).
- **Restock alerts**: an endpoint that lists products at or below their minimum stock level.
- **Money stored in cents** (`long`), never in floating point → no rounding errors.
- **Consistent errors** in RFC-7807 format (`ProblemDetail`).

## The app (live UI)
Besides the API, the project includes a **web application** (plain HTML/CSS/JS, no libraries) that consumes it from the same origin:
- **Dashboard** with KPIs (inventory value in $MXN, units, alerts).
- **Inventory**: create, edit and delete products, search, pagination, stock traffic-light indicator and CSV export.
- **Movements**: global audit feed with filters (stock in/out).
- **Reports**: hand-rolled SVG charts + server-side aggregated metrics (`/api/stats`).
- **Command palette** (Ctrl/Cmd + K), light/dark theme and adjustable density.

---

## Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/productos?buscar=&page=&size=` | List (search + pagination) |
| `GET` | `/api/productos/{id}` | Get a single product |
| `POST` | `/api/productos` | Create (201 + `Location`; 409 on duplicate SKU) |
| `PUT` | `/api/productos/{id}` | Update product data |
| `DELETE` | `/api/productos/{id}` | Delete (204) |
| `POST` | `/api/productos/{id}/entrada` | Stock in (+ audit record) |
| `POST` | `/api/productos/{id}/salida` | Stock out (**409** if not enough stock) |
| `GET` | `/api/productos/{id}/movimientos` | Movement history per product |
| `GET` | `/api/productos/bajo-stock` | Restock alerts |
| `GET` | `/api/movimientos` | Global audit feed (latest movements) |
| `GET` | `/api/stats` | Aggregated inventory summary (server-side) |

Interactive docs at **`/swagger-ui.html`** · OpenAPI spec at **`/v3/api-docs`**.

## Try it (curl)
```bash
BASE=http://localhost:8080

# Create
curl -s -X POST $BASE/api/productos -H "Content-Type: application/json" \
  -d '{"sku":"LAP-900","nombre":"Laptop 14\"","precioCentavos":1299900,"stockInicial":5,"stockMinimo":2}'

# Stock out exceeding available stock -> 409
curl -s -X POST $BASE/api/productos/1/salida -H "Content-Type: application/json" \
  -d '{"cantidad":999,"motivo":"Venta"}'

# Low-stock alerts
curl -s $BASE/api/productos/bajo-stock
```

---

## Run locally
Requires **JDK 17+**. The database is **in-memory H2** (nothing to install).
```bash
./mvnw spring-boot:run
# then open http://localhost:8080
```

## Tests
```bash
./mvnw test
```
Suite with JUnit 5 + MockMvc (creation 201, 404, validation 400, **409 on insufficient stock**). Validated in **CI (GitHub Actions)** on every push.

## Stack
Java 17 · Spring Boot 3.5 (Web, Data JPA, Validation) · H2 / HikariCP · springdoc-openapi (Swagger UI) · Maven · JUnit 5 · Docker.

## Active development branches
- **`security/hardening`** — session login with RBAC roles, brute-force guard, suppliers module and file attachments. Attack-tested, CI green, pending merge.
- **`feature/efactura`** — CFDI 4.0 electronic invoicing for Mexico via a PAC sandbox (work in progress).

## Deploy (Render, free)
1. Push this repo to GitHub.
2. On [render.com](https://render.com) → **New → Web Service** → connect the repo → Render auto-detects the `Dockerfile` (or use **Blueprint** with `render.yaml`).
3. **Free** plan. Render assigns the port via `$PORT` (already configured). Health check: `/actuator/health`.
4. Copy the public URL into your README and proposals.

---

**Author:** Nemeles — Backend Engineer (Java · Spring Boot · REST APIs · SQL). GitHub: **NemelesUrek**.
