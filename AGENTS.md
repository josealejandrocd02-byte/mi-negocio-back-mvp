# AGENTS.md

## Qué es este proyecto (negocio)

**Mi Negocio** es el backend de un SaaS multi-sucursal para negocios pequeños. Funcionalmente permite, **por sucursal**: gestionar un **catálogo** (categorías + productos), **registrar pedidos** de clientes (nombre, notas, varios productos con cantidad) y **registrar ventas** (derivadas de un pedido confirmado, o directas en mostrador), además de consultar **ventas por día/rango**. El dueño de varias sucursales obtiene un resumen consolidado; cada sucursal opera aislada.

Principio central: **aislamiento estricto por sucursal**. Todo dato de negocio (productos, categorías, pedidos, ventas) pertenece a una `store` y nunca es visible ni usable desde otra; por eso casi toda búsqueda por id es `...AndStore_Id`.

El dominio y las reglas oficiales están en `../ieee830.docx` (IEEE 830, en español); el código usa **español** en mensajes de error y nombres de negocio, pero **inglés** en código/entidades.

Modelo de roles:
- `users.platform_role` = `SUPER_ADMIN` (dueño del SaaS) | `USER`.
- `store_users.role` = `STORE_ADMIN` | `EMPLOYEE` (rol **por sucursal**, tabla `store_users`).
- Un `USER` puede pertenecer a varias sucursales con roles distintos. El `SUPER_ADMIN` solo administra la plataforma (`/api/admin/**`), **no** opera dentro de una sucursal.

## Mapa de áreas del negocio y estado

| Área | RF | Estado | Código |
| --- | --- | --- | --- |
| Acceso y roles (login JWT) | RF-01..05 | Implementado | `AuthController`, `jwtconfig/` |
| Sucursales (crear/suspender/cerrar/reactivar, dueño, admins) | RF-06..14 | Implementado | `AdminStoreController` + `service/admin/AdminStoreService` |
| Equipo de sucursal (empleados) y usuarios | RF-15..18 | Implementado | `StoreEmployeeController`/`StoreEmployeeService`; usuarios globales en `service/admin/AdminUserService` |
| **Catálogo (categorías + productos)** | RF-19..23 | **Implementado y con suite QA** | sección siguiente |
| Pedidos | RF-24..28 | **NO implementado** | solo `entity/` + `repository/` |
| Ventas | RF-29..35 | **NO implementado** | solo `entity/` + `repository/` |
| Reglas generales (autorización por sucursal) | RF-36..38 | Implementado | `CatalogAccess`, `StoreUserRepository` |

## Catálogo (categorías + productos)

Implementado (RF-19..RF-23):
- `controller/CategoryController` + `controller/ProductController`
- `service/CategoryService`, `service/ProductService`, `service/CatalogAccess`
- `dtos/catalog/*`, `util/CatalogMapper`

Endpoints (store-scoped; el actor sale del token, no del body):
- `GET/POST /api/stores/{storeId}/categories`, `PUT /{categoryId}`, `PATCH /{categoryId}/active`
- `GET/POST /api/stores/{storeId}/products` (`?categoryId=&q=`; el empleado siempre recibe solo activos), `GET/PUT /{productId}`, `PATCH /{productId}/active`
- `{ "active": false }` es el body de los PATCH.
- Crear producto: `{ name, categoryId, price, active }`; editar producto: `{ name, categoryId, price }` (el `active` se cambia con PATCH). Crear categoría: `{ name, active }`; editar: `{ name }`.

Patrón obligatorio en esta área: el controller delega en el `service`; el service llama `CatalogAccess.requireManager` (escritura) o `requireMember` (lectura) **antes** de tocar datos y mapea con `CatalogMapper`. No metas autorización en el controller ni accedas a repositorios desde él.

Reglas NO negociables de esta área (vienen del IEEE 830, romperlas es un bug):
- **Sin control de stock** (RF-21). `Product.stockQuantity` se eliminó a propósito. No reintroducir inventario, disponibilidad, ni descuentos. Producto agotado = `active = false`.
- **Nunca borrado físico** de productos/categorías: solo `PATCH .../active`. `Category` tiene `is_active`; `Product` tiene `is_active`.
- `STORE_ADMIN` gestiona; `EMPLOYEE` **solo lee productos activos** y no modifica (RF-22). Además, un producto cuya categoría está inactiva **no se le muestra al empleado** (se filtra en `ProductRepository.findForList` para el listado y en `ProductService.get` para el detalle).
- **Nombre único por sucursal** (case-insensitive) para categorías y productos, y **precio > 0** en productos. Se valida en el service (`existsByStore_IdAndNameIgnoreCase...`) y en BD con los índices `ux_products_store_name`/`ux_categories_store_name` (`lower(name)`); una violación de integridad se mapea a 409 en `GlobalExceptionHandler`.
- Aislamiento por sucursal (RF-23): toda búsqueda por id usa `findByIdAndStore_Id`/`findDetail`; la categoría de un producto debe ser de la **misma** sucursal.
- Endpoints store-scoped: `/api/stores/{storeId}/categories` y `/api/stores/{storeId}/products` (`?categoryId=&q=`). La foto del producto vive aparte en `/api/stores/{storeId}/foto-config/products/{productId}/photo`.
- **Fotos**: el logo lo gestiona SUPER_ADMIN o STORE_ADMIN; la **foto de producto solo STORE_ADMIN** (sin bypass de superadmin).
- Los listados de productos usan `ProductRepository.findForList` (un solo query con `left join fetch p.category`, filtros en BD y `order by name`). No reintroducir los `findByStore_Id*` de productos que provocaban N+1. El filtro `q` sí se aplica en memoria.
- `superadmin` NO gestiona catálogo (decisión explícita); solo `/api/admin/**`.
- **Migraciones de BD**: v3/v4 ya están aplicadas en la BD local. v3 = `is_active` en categorías y `DROP stock_quantity`; v4 = `price > 0`, `created_at` en categorías, índices por categoría y unicidad `lower(name)`. Al recrear la BD hay que aplicarlas manualmente: `ddl-auto=none` no las ejecuta. Antes de v4, revisar duplicados y `price = 0`.

### Gotchas del catálogo (verificados; no romper)

- `ProductService.create` usa `saveAndFlush` a propósito: con `save()` el `201` devuelve `createdAt: null`.
- `CategoryResponse` solo expone `id`, `name` y `active`; no tiene `createdAt`.
- `SecurityConfig` debe permitir `/error`; si se quita, los 400/404/405 del framework se convierten en 401.
- El JWT lleva email (subject), `userId` y `platformRole`; **no** lleva roles por sucursal: se resuelven en BD en cada request.
- Códigos esperados: sin membresía en el `storeId` del path -> 403; recurso de otra sucursal -> 404; DELETE de catálogo -> 405; SUPER_ADMIN en catálogo -> 403; sucursal SUSPENDED/CLOSED -> 409.
- No existe endpoint para que un miembro liste sus sucursales; el `storeId` se obtiene de la BD o de `/api/admin/stores` como SUPER_ADMIN.
- Tienda Central puede tener fixtures QA inactivos de corridas anteriores. No borrarlos físicamente; usar un `runId` nuevo.

## Aspectos del negocio pendientes de implementar

- **Pedidos y ventas**: existen entidades y repositorios (`CustomerOrder` tabla `orders`, `OrderItem`, `Sale`, `SaleItem`) pero **no hay controllers ni services todavía**. Al implementarlos, ojo: `OrderStatus` actual es `PENDING, PREPARING, READY, DELIVERED, CANCELLED`, pero el IEEE 830 pide `pendiente / confirmado("vendido") / cancelado` — revisar/renombrar.
- Fuera de alcance (RF): control de stock, métodos de pago, anulación de ventas, base de clientes, proveedores/compras, caja diaria, facturación, reportes avanzados.

## Arquitectura

Spring Boot **4.1.1**, Java **21**, Maven. Capas: `controller` -> `service` -> `repository` (Spring Data JPA), `entity` (JPA), `dtos`, `emus` (así, con typo), `util`, `jwtconfig`, `cloudinary`, `exception`.

- Paquete base `com.minegocio.mi_negocio` (con **underscore**: el nombre original `mi-negocio` era inválido).
- **La identidad de quien opera sale SIEMPRE del token**, vía `@AuthenticationPrincipal JwtPrincipal` (`getUserId()`, `isSuperAdmin()`). Nunca del body (RF-37).
- `SecurityConfig`: `permitAll` `/api/auth/**`, `/api/public/**` y `/error`; `/api/admin/**` exige `ROLE_SUPER_ADMIN`. El resto exige token. **La autorización por sucursal NO está en SecurityConfig**, se valida en los servicios con `StoreUserRepository` / `CatalogAccess` (`requireManager` / `requireMember`, y siempre sucursal `ACTIVE`).
- Errores: usar `util/ApiErrors` (`notFound`, `conflict`, `forbidden`, `badRequest`) que lanzan `ResponseStatusException`; `GlobalExceptionHandler` los formatea. Las violaciones de unicidad en BD (`DataIntegrityViolationException`) se mapean a 409.
- DTOs de respuesta inmutables (campos `final` + constructor). Mappers manuales en `util` (ej. `StoreMapper`, `CatalogMapper`).
- Imágenes: Cloudinary (`CloudinaryService`, `StoreFotoConfigService`); borrado diferido `afterCommit`.
- El SQL del esquema **no es un `.sql`**: es un comentario en `MiNegocioApplication.java`. `spring.jpa.hibernate.ddl-auto=none`, así que cambios de esquema = editar ese bloque **y** aplicar el `ALTER`/`CREATE` manualmente a la BD.

## Comandos

Requiere **JDK 21**. En esta máquina el `java` del PATH es un **JRE** sin `javac`; hay que fijar `JAVA_HOME`:

```powershell
$env:JAVA_HOME="C:\Users\HP\.jdks\corretto-21.0.8"
.\mvnw.cmd -q -o compile   # compila (offline); quita -o si falta algún artefacto
.\mvnw.cmd spring-boot:run  # arranca (ver config abajo)
```

- `MiNegocioApplicationTests` es solo context load y **necesita la BD arriba**; no cubre reglas de negocio.
- La verificación real es la suite Postman/Newman de `src/test/resources/postman/` (ver sección QA).
- No hay linter/formatter configurado.

## Testing / QA

Requiere Node/npx, la app en `9080` y PostgreSQL arriba. La suite crea datos efímeros con un `runId`.

```powershell
# Suite completa (RF-01..23, RF-36..38)
node src/test/resources/postman/build-collection.js r20261021120000
npx --yes newman run src/test/resources/postman/MiNegocio-QA.postman_collection.json `
  -e src/test/resources/postman/local.postman_environment.json --reporters cli,json

# Suite de la cuenta real admin@ en Tienda Central (RF-19..23, RF-36..37)
node src/test/resources/postman/build-admin-collection.js r20261021120000
npx --yes newman run src/test/resources/postman/AdminCuenta-QA.postman_collection.json `
  -e src/test/resources/postman/admin-cuenta.local.postman_environment.json --reporters cli,json
```

- **Usa un `runId` nuevo en cada corrida**; reutilizarlo choca con emails, nombres y sucursales QA ya creados.
- Edita `spec/*.js` y `build-*.js`; los `*.postman_collection.json` son artefactos generados.
- **No commitees los `*.postman_environment.json`**: tienen contraseñas en texto plano. Los builders también tienen credenciales locales hardcodeadas; sanitizar antes de publicar.
- La suite completa crea/cierra sucursales y usuarios QA; su teardown los deja inactivos/cerrados. La suite admin usa Tienda Central: **no** la suspendas/cierres ni bloquees `admin@`; solo desactiva fixtures.
- La carpeta `08-photos` depende de Cloudinary; sin credenciales/red puede fallar y no es un fallo del catálogo.

## Config / entorno

- Puerto `9080`. PostgreSQL local: `jdbc:postgresql://localhost:5432/minegocio`, user `postgres`, pass `5432` (`src/main/resources/application.properties`).
- psql local: `C:\Program Files\PostgreSQL\15\bin\psql.exe`; usa `$env:PGPASSWORD="5432"` y `-w` para evitar el prompt.
- JWT: expira a 24h; secreto por env `JWT_SECRET_KEY` (hay default de dev).
- CORS permitido: `localhost:8081` (Expo/RN), `8080`, `3000`.
- Hay `opencode.json` con MCP `context7`; reiniciar OpenCode tras cambiarlo. No imprimas su contenido: incluye una API key.
- El login (`POST /api/auth/login`) devuelve `{ token, tokenType, userId, email, fullName, platformRole }`.
- Usuarios de prueba (password `Admin123*`): `superadmin@minegocio.com` (SUPER_ADMIN), `carlos@minegocio.com` (STORE_ADMIN de Tienda Central) y `maria@minegocio.com` (EMPLOYEE); están en los INSERTs al final del bloque SQL. `admin@minegocio.com` (STORE_ADMIN de Tienda Central) también existe en la BD local y lo usa la suite admin.
