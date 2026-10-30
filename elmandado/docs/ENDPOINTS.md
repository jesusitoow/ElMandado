# ENDPOINTS - El Mandado

## Descripción

El Mandado expone dos interfaces sobre la misma lógica de negocio:

* **API REST** (`PedidoController`): trabaja con JSON bajo `/api/pedidos`.
* **Vistas web** (`PedidoViewController`): páginas HTML con Thymeleaf bajo `/pedidos`.

Documentación relacionada: [`EXCEPTIONS.md`](EXCEPTIONS.md) (formato de los errores) y [`VIEW_CONTROLLER.md`](VIEW_CONTROLLER.md) (detalle de las vistas).

---

# PARTE 1: API REST

Base URL:

```text
http://localhost:8080/api/pedidos
```

| Método   | Endpoint                     | Función               | Éxito |
| -------- | ---------------------------- | --------------------- | ----- |
| `GET`    | `/api/pedidos`               | Listar pedidos        | `200` |
| `GET`    | `/api/pedidos/{id}`          | Consultar un pedido   | `200` |
| `POST`   | `/api/pedidos`               | Crear un pedido       | `201` |
| `PUT`    | `/api/pedidos/{id}`          | Actualizar un pedido  | `200` |
| `PUT`    | `/api/pedidos/{id}/entregar` | Marcar como entregado | `204` |
| `DELETE` | `/api/pedidos/{id}`          | Eliminar un pedido    | `204` |

---

## 1. Listar todos los pedidos

```http
GET /api/pedidos
```

**Respuesta: `200 OK`**

```json
[
  {
    "id": 1,
    "cliente": "Carlos",
    "plato": "Pizza Pepperoni",
    "precio": 12.5,
    "entregado": false
  },
  {
    "id": 2,
    "cliente": "Ana",
    "plato": "Hamburguesa Doble",
    "precio": 9.8,
    "entregado": true
  }
]
```

---

## 2. Consultar pedido por ID

```http
GET /api/pedidos/1
```

**Respuesta: `200 OK`**

```json
{
  "id": 1,
  "cliente": "Carlos",
  "plato": "Pizza Pepperoni",
  "precio": 12.5,
  "entregado": false
}
```

**Pedido no encontrado: `404 Not Found`**

```json
{ "error": "No existe el pedido con ID: 99" }
```

---

## 3. Crear pedido

```http
POST /api/pedidos
```

**Body**

```json
{
  "cliente": "Luis",
  "plato": "Tacos al Pastor",
  "precio": 8.5
}
```

**Respuesta: `201 Created`**

```json
{
  "id": 3,
  "cliente": "Luis",
  "plato": "Tacos al Pastor",
  "precio": 8.5,
  "entregado": false
}
```

**Error de validación: `400 Bad Request`**

Un mensaje por cada campo que falla:

```json
{
  "cliente": "El cliente es obligatorio",
  "precio": "El precio debe ser positivo"
}
```

---

## 4. Actualizar pedido

Reemplaza `cliente`, `plato` y `precio`. El estado `entregado` no cambia con este endpoint.

```http
PUT /api/pedidos/1
```

**Body**

```json
{
  "cliente": "Carlos",
  "plato": "Pizza Hawaiana",
  "precio": 13.0
}
```

**Respuesta: `200 OK`**

```json
{
  "id": 1,
  "cliente": "Carlos",
  "plato": "Pizza Hawaiana",
  "precio": 13.0,
  "entregado": false
}
```

**Errores**

* `400 Bad Request`: datos inválidos (mismo formato que en crear).
* `404 Not Found`: el pedido no existe.

---

## 5. Marcar pedido como entregado

```http
PUT /api/pedidos/1/entregar
```

**Respuesta: `204 No Content`** (sin cuerpo)

**Pedido ya entregado: `400 Bad Request`**

```json
{ "error": "El pedido con ID 1 ya había sido marcado como entregado" }
```

**Pedido no encontrado: `404 Not Found`**

```json
{ "error": "No existe el pedido con ID: 99" }
```

---

## 6. Eliminar pedido

```http
DELETE /api/pedidos/1
```

**Respuesta: `204 No Content`** (sin cuerpo)

**Pedido no encontrado: `404 Not Found`**

```json
{ "error": "No existe el pedido con ID: 99" }
```

---

## Códigos HTTP utilizados

| Código                      | Significado                                    |
| --------------------------- | ---------------------------------------------- |
| `200 OK`                    | Consulta o actualización realizada correctamente |
| `201 Created`               | Recurso creado exitosamente                    |
| `204 No Content`            | Operación realizada sin contenido de respuesta |
| `400 Bad Request`           | Datos inválidos o regla de negocio incumplida  |
| `404 Not Found`             | Recurso solicitado no existe                   |
| `500 Internal Server Error` | Error no previsto en el servidor               |

## Formatos de error

| Origen | Formato |
|--------|---------|
| Recurso no encontrado / regla de negocio | `{ "error": "<mensaje>" }` |
| Validación del DTO | `{ "<campo>": "<mensaje>", ... }` |
| Error no previsto | `{ "error": "...", "detalle": "<mensaje>" }` |

---

# PARTE 2: Vistas web

Todas las rutas devuelven HTML (o una redirección). No devuelven JSON.

| Método | Ruta                       | Función                          | Resultado |
| ------ | -------------------------- | -------------------------------- | --------- |
| `GET`  | `/`                        | Página de inicio                 | `index.html` |
| `GET`  | `/pedidos`                 | Lista de pedidos                 | `pedidos/lista` |
| `GET`  | `/pedidos/nuevo`           | Formulario vacío                 | `pedidos/formulario` |
| `POST` | `/pedidos`                 | Guardar un pedido nuevo          | Redirige a `/pedidos` |
| `GET`  | `/pedidos/{id}/editar`     | Formulario con los datos cargados | `pedidos/formulario` |
| `POST` | `/pedidos/{id}`            | Guardar la edición               | Redirige a `/pedidos` |
| `POST` | `/pedidos/{id}/entregar`   | Marcar como entregado            | Redirige a `/pedidos` |
| `POST` | `/pedidos/{id}/eliminar`   | Eliminar un pedido               | Redirige a `/pedidos` |

Las acciones que modifican datos usan `POST` porque los formularios HTML solo soportan `GET` y `POST`.

Si los datos del formulario no son válidos, **no se redirige**: se vuelve a mostrar `pedidos/formulario` con el mensaje de error de cada campo y lo que el usuario había escrito. Esto lo hace `VistaExceptionHandler`.

---

# Arquitectura aplicada

* **Modelo (`Pedido`):** entidad JPA con la información de cada pedido.
* **Controladores (`PedidoController`, `PedidoViewController`):** reciben las peticiones y delegan en el servicio.
* **Servicio (`PedidoService`):** lógica de negocio y conversión a DTOs.
* **Repositorio (`PedidoRepository`):** acceso a la base de datos con Spring Data JPA.
* **Manejadores de excepciones:** `GlobalExceptionHandler` (API) y `VistaExceptionHandler` (vistas).

Se usa inyección de dependencias por constructor para desacoplar los componentes.