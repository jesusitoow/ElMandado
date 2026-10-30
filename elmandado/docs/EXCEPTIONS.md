# Manejo de Excepciones - El Mandado

## Descripción

El Mandado separa **quién detecta el error** de **cómo se le muestra al usuario**:

* El **servicio** solo lanza excepciones propias y no decide el código HTTP.
* Un **manejador** (`@ControllerAdvice`) intercepta la excepción y arma la respuesta.

Como la aplicación tiene dos tipos de cliente, hay **dos manejadores**:

| Manejador | Anotación | Cliente | Respuesta |
|-----------|-----------|---------|-----------|
| `GlobalExceptionHandler` | `@RestControllerAdvice` | API REST | JSON con código HTTP |
| `VistaExceptionHandler` | `@ControllerAdvice(assignableTypes = PedidoViewController.class)` | Vistas web | Formulario HTML con errores |

```text
exepciones
│
├── RecursoNoEncontradoException
├── ValidacionException
├── GlobalExceptionHandler
└── VistaExceptionHandler
```

> El paquete se llama `exepciones` (tal cual está en el código).

---

# Excepciones propias

Ambas extienden `RuntimeException` (no son *checked*, no hay que declararlas con `throws`) y solo reciben un mensaje:

```java
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

```java
public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
```

| Excepción | Cuándo se lanza | Respuesta HTTP (API) |
|-----------|-----------------|----------------------|
| `RecursoNoEncontradoException` | El pedido con ese ID no existe | `404 Not Found` |
| `ValidacionException` | Se rompe una regla de negocio | `400 Bad Request` |

---

# GlobalExceptionHandler (API)

Clase anotada con `@RestControllerAdvice`. Intercepta las excepciones lanzadas desde los controladores (o desde las capas que estos llaman) y las convierte en respuestas JSON.

| Excepción capturada | Código | Cuerpo de la respuesta |
|---------------------|--------|------------------------|
| `RecursoNoEncontradoException` | `404 Not Found` | `{ "error": "<mensaje>" }` |
| `ValidacionException` | `400 Bad Request` | `{ "error": "<mensaje>" }` |
| `MethodArgumentNotValidException` | `400 Bad Request` | `{ "<campo>": "<mensaje>", ... }` |
| `Exception` (cualquier otra) | `500 Internal Server Error` | `{ "error": "...", "detalle": "<mensaje>" }` |

## 1. Recurso no encontrado

```java
@ExceptionHandler(RecursoNoEncontradoException.class)
public ResponseEntity<Map<String, String>> handleNoEncontrado(RecursoNoEncontradoException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", ex.getMessage()));
}
```

```json
{ "error": "No existe el pedido con ID: 99" }
```

## 2. Reglas de negocio

```java
@ExceptionHandler(ValidacionException.class)
public ResponseEntity<Map<String, String>> handleValidacion(ValidacionException ex) {
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
}
```

```json
{ "error": "El pedido con ID 1 ya había sido marcado como entregado" }
```

## 3. Validaciones del DTO (`@Valid`)

Cuando `CrearPedidoDTO` incumple sus anotaciones, Spring lanza `MethodArgumentNotValidException` **antes de entrar al servicio**. El handler arma un mapa `campo → mensaje`:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, String>> handleValidacionDTO(MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
            .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
}
```

```json
{
  "cliente": "El cliente es obligatorio",
  "precio": "El precio debe ser positivo"
}
```

El formato es distinto al de los otros errores porque pueden fallar varios campos a la vez.

## 4. Errores no previstos

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                    "error", "Ha ocurrido un error interno en el servidor",
                    "detalle", ex.getMessage() != null ? ex.getMessage() : "Error no especificado"));
}
```

Es la red de seguridad final. Spring elige siempre el handler **más específico**, por eso este solo se usa como último recurso.

---

# VistaExceptionHandler (vistas web)

Las vistas Thymeleaf no pueden responder con JSON: si el usuario escribe datos inválidos en el formulario, lo correcto es **volver a mostrar el formulario** con los mensajes de error y conservando lo que escribió.

```java
@ControllerAdvice(assignableTypes = PedidoViewController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class VistaExceptionHandler {
```

| Elemento | Qué hace |
|----------|----------|
| `@ControllerAdvice` | Manejador global de controladores que **no** son REST (permite devolver una vista). |
| `assignableTypes = PedidoViewController.class` | Limita el manejador a ese controlador; la API no se ve afectada. |
| `@Order(Ordered.HIGHEST_PRECEDENCE)` | Le da prioridad sobre `GlobalExceptionHandler`, para que las validaciones del formulario no terminen como JSON. |

## Cómo funciona

```java
@ExceptionHandler(BindException.class)
public ModelAndView handleValidacionFormulario(BindException ex, HttpServletRequest request) {
    ModelAndView mav = new ModelAndView("pedidos/formulario");
    mav.addAllObjects(ex.getBindingResult().getModel());
    ...
}
```

1. Captura `BindException`, que se lanza cuando falla `@Valid` sobre un `@ModelAttribute`.
2. Crea un `ModelAndView` apuntando a la plantilla `pedidos/formulario`.
3. `ex.getBindingResult().getModel()` incluye el DTO con lo que escribió el usuario (`nuevoPedido`) y el `BindingResult` con los errores de cada campo, que Thymeleaf usa para mostrarlos junto a cada input.

## ¿Creando o editando?

El mismo formulario sirve para crear y editar. El handler lo deduce mirando la URL:

```java
Map<String, String> variables = (Map<String, String>) request
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

boolean esEdicion = variables != null && variables.containsKey("id");
mav.addObject("esEdicion", esEdicion);
if (esEdicion) {
    mav.addObject("idPedido", Long.valueOf(variables.get("id")));
}
```

| Petición | ¿Tiene `{id}`? | `esEdicion` |
|----------|----------------|-------------|
| `POST /pedidos` | No | `false` (crear) |
| `POST /pedidos/5` | Sí | `true` (editar), `idPedido = 5` |

Así la plantilla sabe a qué URL enviar el formulario y qué título mostrar.

### Flujo de un error de validación en el formulario

```text
POST /pedidos/5   (precio = -3)
        ↓
@Valid en PedidoViewController.actualizar() falla
        ↓
Spring lanza BindException
        ↓
VistaExceptionHandler.handleValidacionFormulario()
        ↓
Vuelve a mostrar pedidos/formulario con:
  - lo que escribió el usuario
  - el mensaje de error junto a "precio"
  - esEdicion = true, idPedido = 5
```

---

# Qué manejador atiende cada caso

| Situación | Origen | Quién responde |
|-----------|--------|----------------|
| JSON inválido en `POST /api/pedidos` | `PedidoController` | `GlobalExceptionHandler` → `400` JSON |
| Formulario inválido en `POST /pedidos` | `PedidoViewController` | `VistaExceptionHandler` → formulario HTML |
| Pedido inexistente en la API | Servicio | `GlobalExceptionHandler` → `404` JSON |
| Pedido inexistente en una vista (`/pedidos/99/editar`) | Servicio | `GlobalExceptionHandler` → `404` JSON |
| Marcar entregado dos veces (API o vista) | Servicio | `GlobalExceptionHandler` → `400` JSON |

## Limitación actual

`VistaExceptionHandler` solo maneja `BindException` (validación de formularios). Los errores que lanza el servicio (`RecursoNoEncontradoException`, `ValidacionException`) siguen siendo atendidos por `GlobalExceptionHandler`, porque `@RestControllerAdvice` aplica a **todos** los controladores. Por eso, si desde el navegador se abre `/pedidos/99/editar` con un ID inexistente, se ve el JSON de error en lugar de una página HTML.

Una mejora futura sería agregar en `VistaExceptionHandler` handlers para esas dos excepciones que muestren una página de error amigable.

---

# Principio aplicado

```text
Servicio  → lanza la excepción (no conoce HTTP ni HTML)
Manejador → decide cómo se muestra (JSON o formulario)
```

Los controladores quedan limpios: no tienen `try/catch`.
