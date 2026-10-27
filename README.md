# El Mandado

## Descripción

El Mandado es una aplicación desarrollada con **Java y Spring Boot** que permite gestionar los pedidos de una tienda de domicilios. Ofrece dos formas de usarla sobre la misma lógica de negocio:

* Una **API REST** que trabaja con JSON.
* Una **interfaz web** con páginas HTML (plantillas Thymeleaf) para gestionar los pedidos desde el navegador.

El sistema permite:

* Registrar pedidos.
* Consultar todos los pedidos.
* Buscar pedidos por identificador.
* Actualizar pedidos.
* Marcar pedidos como entregados.
* Eliminar pedidos.

El proyecto aplica una arquitectura por capas y conceptos fundamentales de Spring Boot: inyección de dependencias, separación de responsabilidades, DTOs, validación de datos, manejo centralizado de excepciones (uno para la API y otro para las vistas), persistencia con Spring Data JPA y APIs RESTful.

---

## Objetivo

Desarrollar una aplicación que permita administrar el ciclo básico de los pedidos de una tienda de domicilios, aplicando los conceptos de Spring Boot vistos durante el desarrollo de la asignatura.

---

## Tecnologías utilizadas

* **Java**
* **Spring Boot**
* **Spring Web (Spring MVC)**
* **Spring Data JPA**
* **Thymeleaf**
* **Jakarta Bean Validation**
* **Lombok**
* **Maven**
* **HTML y CSS**
* **JSON**
* **Git / GitHub**

---

## Accesos rápidos

Con la aplicación en ejecución:

| Acceso | URL |
|--------|-----|
| Página de inicio | `http://localhost:8080` |
| Vistas web (gestión de pedidos) | `http://localhost:8080/pedidos` |
| API REST | `http://localhost:8080/api/pedidos` |

---

## Novedades de esta versión

| Área | Cambio |
|------|--------|
| Persistencia | `PedidoRepository` ahora es una interfaz **Spring Data JPA**. Se eliminó `PedidoRepositoryMemoria`. |
| Servicio | Usa **Lombok** (`@RequiredArgsConstructor`), **`@Transactional`** y métodos auxiliares (`buscarOFallar`, `aDTO`). |
| Excepciones | Nuevo **`VistaExceptionHandler`** para mostrar los errores de validación dentro del formulario HTML. |
| Estructura | La carpeta Maven pasó de `foodexpress` a **`elmandado`**; `index.html` ahora está en `templates`. |
| Código | Se retiraron los comentarios de los archivos Java; la explicación vive ahora en la documentación. |
| Documentación | Nuevos documentos: `EXCEPTIONS.md`, `VIEW_CONTROLLER.md` y `REPOSITORY.md`. |

---

## Arquitectura del proyecto

```text
com.elmandado
│
├── controlador
│   ├── PedidoController
│   └── PedidoViewController
│
├── dto
│   ├── CrearPedidoDTO
│   └── PedidoDTO
│
├── servicio
│   ├── PedidoService
│   └── PedidoServiceImpl
│
├── repositorio
│   └── PedidoRepository
│
├── modelo
│   └── Pedido
│
├── exepciones
│   ├── GlobalExceptionHandler
│   ├── VistaExceptionHandler
│   ├── RecursoNoEncontradoException
│   └── ValidacionException
│
└── elmandadoApplication
```

```text
Controlador → Servicio → Repositorio → Base de datos
```

### Modelo

La clase `Pedido` representa la información de cada pedido y es la entidad que persiste JPA.

### DTOs

Los DTOs separan lo que entra y sale de la aplicación del modelo interno:

* `CrearPedidoDTO`: datos de entrada para crear y actualizar un pedido. Incluye las validaciones de formato.
* `PedidoDTO`: datos de salida que se devuelven al cliente.

### Controladores

* `PedidoController`: API REST bajo `/api/pedidos`. Devuelve JSON.
* `PedidoViewController`: vistas web bajo `/pedidos`. Devuelve páginas HTML y redirecciones.

Ninguno de los dos contiene lógica de negocio ni manejo de excepciones.

### Servicio

`PedidoService` y `PedidoServiceImpl` contienen las operaciones y reglas de negocio, y convierten entre la entidad `Pedido` y los DTOs. Las operaciones de escritura son transaccionales.

### Repositorio

`PedidoRepository` extiende `JpaRepository<Pedido, Long>`. Spring Data genera la implementación en tiempo de ejecución, y unos métodos `default` conservan los nombres en español que usa el servicio.

### Excepciones

* `RecursoNoEncontradoException`: el pedido solicitado no existe.
* `ValidacionException`: se incumple una regla de negocio.
* `GlobalExceptionHandler`: traduce las excepciones a respuestas JSON (API).
* `VistaExceptionHandler`: vuelve a mostrar el formulario con los errores de cada campo (vistas web).

---

## Documentación

Información detallada sobre cada parte del proyecto (carpeta `elmandado/docs`):

* 📌 **[`ENDPOINTS.md`](elmandado/docs/ENDPOINTS.md)**
  API REST y vistas web: rutas, métodos HTTP, cuerpos JSON, errores y códigos de respuesta.

* ⚙️ **[`SERVICE.md`](elmandado/docs/SERVICE.md)**
  Capa de servicio, DTOs, validaciones, transacciones y comunicación con el repositorio.

* 🛡️ **[`EXCEPTIONS.md`](elmandado/docs/EXCEPTIONS.md)**
  Excepciones propias y los dos manejadores: `GlobalExceptionHandler` (API) y `VistaExceptionHandler` (vistas).

* 🖥️ **[`VIEW_CONTROLLER.md`](elmandado/docs/VIEW_CONTROLLER.md)**
  El controlador de las vistas web (`PedidoViewController`), sus rutas, el flujo de formularios y el patrón Post/Redirect/Get.

* 🗄️ **[`REPOSITORY.md`](elmandado/docs/REPOSITORY.md)**
  El repositorio con Spring Data JPA y los métodos que usa el servicio.

* 🗂️ **[`STRUCTURE.md`](elmandado/docs/STRUCTURE.md)**
  Estructura de carpetas y archivos del proyecto y la responsabilidad de cada uno.

---

## Modelo de datos

Cada pedido contiene:

| Campo       | Tipo      | Descripción                       |
| ----------- | --------- | --------------------------------- |
| `id`        | `Long`    | Identificador del pedido (lo asigna el sistema) |
| `cliente`   | `String`  | Nombre del cliente                |
| `plato`     | `String`  | Plato solicitado                  |
| `precio`    | `Double`  | Precio del pedido                 |
| `entregado` | `boolean` | Indica si el pedido fue entregado (empieza en `false`) |

Ejemplo:

```json
{
  "id": 1,
  "cliente": "Carlos",
  "plato": "Pizza Pepperoni",
  "precio": 12.5,
  "entregado": false
}
```

### Validaciones

Los datos de entrada (`CrearPedidoDTO`) se validan automáticamente:

| Campo     | Reglas                            |
| --------- | --------------------------------- |
| `cliente` | Obligatorio, máximo 25 caracteres |
| `plato`   | Obligatorio, máximo 50 caracteres |
| `precio`  | Obligatorio, mayor a 0            |

Además, el servicio aplica reglas de negocio, por ejemplo: un pedido que ya fue entregado no puede marcarse como entregado nuevamente.

---

## Endpoints principales

Dirección base de la API:

```text
http://localhost:8080/api/pedidos
```

| Método   | Endpoint                     | Función               |
| -------- | ---------------------------- | --------------------- |
| `POST`   | `/api/pedidos`               | Crear un pedido       |
| `GET`    | `/api/pedidos`               | Listar pedidos        |
| `GET`    | `/api/pedidos/{id}`          | Consultar un pedido   |
| `PUT`    | `/api/pedidos/{id}`          | Actualizar un pedido  |
| `PUT`    | `/api/pedidos/{id}/entregar` | Marcar como entregado |
| `DELETE` | `/api/pedidos/{id}`          | Eliminar un pedido    |

Para ejemplos de cada endpoint, consultar **[`ENDPOINTS.md`](elmandado/docs/ENDPOINTS.md)**.

### Vistas web

| Método | Ruta                       | Función                          |
| ------ | -------------------------- | -------------------------------- |
| `GET`  | `/`                        | Página de inicio                 |
| `GET`  | `/pedidos/nuevo`           | Formulario para crear un pedido  |
| `POST` | `/pedidos`                 | Guardar un pedido nuevo          |
| `GET`  | `/pedidos`                 | Lista de pedidos                 |
| `GET`  | `/pedidos/{id}/editar`     | Formulario para editar un pedido |
| `POST` | `/pedidos/{id}`            | Guardar la edición               |
| `POST` | `/pedidos/{id}/entregar`   | Marcar como entregado            |
| `POST` | `/pedidos/{id}/eliminar`   | Eliminar un pedido               |

Las vistas usan `POST` para editar, entregar y eliminar porque los formularios HTML solo soportan `GET` y `POST`. Más detalle en **[`VIEW_CONTROLLER.md`](elmandado/docs/VIEW_CONTROLLER.md)**.

---

## Códigos HTTP

| Código                      | Significado                                         |
| --------------------------- | --------------------------------------------------- |
| `200 OK`                    | Consulta o actualización realizada correctamente    |
| `201 Created`               | Pedido creado correctamente                         |
| `204 No Content`            | Operación realizada correctamente sin contenido     |
| `400 Bad Request`           | Datos inválidos o regla de negocio incumplida       |
| `404 Not Found`             | Pedido no encontrado                                |
| `500 Internal Server Error` | Error no previsto en el servidor                    |

### Formato de los errores

Errores de negocio y recurso no encontrado:

```json
{ "error": "No existe el pedido con ID: 99" }
```

Errores de validación de los datos de entrada (un mensaje por campo):

```json
{
  "cliente": "El cliente es obligatorio",
  "precio": "El precio debe ser positivo"
}
```

En las **vistas web**, los errores de validación no se muestran como JSON: el formulario se vuelve a mostrar con el mensaje junto a cada campo (ver [`EXCEPTIONS.md`](elmandado/docs/EXCEPTIONS.md)).

---

## Almacenamiento

Los pedidos se guardan en una **base de datos relacional** mediante **Spring Data JPA**. A diferencia de la versión anterior (almacenamiento en memoria con `ConcurrentHashMap` y `AtomicLong`), los datos **ya no se pierden al reiniciar** la aplicación.

El motor de base de datos y sus credenciales se definen en `application.properties`. Ver **[`REPOSITORY.md`](elmandado/docs/REPOSITORY.md)**.

---

## Configuración

Archivo:

```text
elmandado/src/main/resources/application.properties
```

Propiedades básicas:

```properties
spring.application.name=elmandado
server.port=8080
```

A estas se suman las propiedades de conexión a la base de datos (`spring.datasource.*`) y de JPA (`spring.jpa.*`), según el motor que se use.

La aplicación estará disponible en:

```text
http://localhost:8080
```

---

## Ejecución del proyecto

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
```

### 2. Entrar al proyecto

```bash
cd elmandado
```

### 3. Ejecutar la aplicación

Con Maven:

```bash
mvn spring-boot:run
```

O con el Maven Wrapper incluido (no requiere tener Maven instalado):

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

También puede ejecutarse desde el IDE utilizando la clase principal de Spring Boot.

---

## Ejemplos de prueba

### Crear un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos" `
-Method Post `
-ContentType "application/json" `
-Body '{"cliente":"Carlos","plato":"Pizza Pepperoni","precio":12.50}'
```

### Consultar pedidos

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos"
```

### Consultar un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1"
```

### Actualizar un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1" `
-Method Put `
-ContentType "application/json" `
-Body '{"cliente":"Carlos","plato":"Pizza Hawaiana","precio":13.00}'
```

### Marcar como entregado

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1/entregar" `
-Method Put
```

### Eliminar un pedido

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/pedidos/1" `
-Method Delete
```

---

## Conceptos de Spring Boot aplicados

* Spring Boot y Spring MVC.
* `@RestController` y `@Controller`.
* `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`.
* `@RequestBody`, `@PathVariable`, `@ResponseStatus`.
* DTOs con `record`.
* Validación con Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Size`, `@Positive`).
* `@ModelAttribute`, `Model`, `ModelAndView` y `BindingResult`.
* Plantillas Thymeleaf y fragmentos reutilizables.
* Patrón Post/Redirect/Get.
* Inyección de dependencias por constructor (manual y con Lombok `@RequiredArgsConstructor`).
* `@Service` y `@Repository`.
* Spring Data JPA (`JpaRepository`) y métodos `default` en interfaces.
* Transacciones con `@Transactional`.
* Interfaces e implementaciones.
* Arquitectura por capas.
* APIs RESTful, JSON y códigos de estado HTTP.
* Excepciones personalizadas.
* Manejo global de excepciones con `@RestControllerAdvice` y `@ExceptionHandler`.
* Manejo de excepciones específico para vistas con `@ControllerAdvice(assignableTypes = ...)` y `@Order`.

---

## Documentación del proyecto

```text
README.md
│
└── elmandado/docs
    ├── ENDPOINTS.md        Rutas de la API y de las vistas web
    ├── SERVICE.md          Capa de servicio, DTOs y transacciones
    ├── EXCEPTIONS.md       Excepciones y manejadores (API y vistas)
    ├── VIEW_CONTROLLER.md  Controlador de las vistas web
    ├── REPOSITORY.md       Repositorio con Spring Data JPA
    └── STRUCTURE.md        Estructura de carpetas y archivos
```

El `README.md` presenta una visión general del proyecto, mientras que los archivos de `docs` contienen información específica sobre su funcionamiento interno.

---

## Autor

Proyecto académico desarrollado como práctica de Spring Boot para comprender la creación de APIs REST, interfaces web con plantillas, persistencia con JPA y la aplicación de una arquitectura por capas.