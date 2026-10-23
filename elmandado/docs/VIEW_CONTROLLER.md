# Controlador de Vistas - PedidoViewController

## Descripción

`PedidoViewController` es el controlador de la **interfaz web** de El Mandado. Atiende las páginas HTML (plantillas Thymeleaf) bajo `/pedidos` y usa el mismo `PedidoService` que la API REST.

```java
@Controller
@RequestMapping("/pedidos")
public class PedidoViewController {

    private final PedidoService servicio;

    public PedidoViewController(PedidoService servicio) {
        this.servicio = servicio;
    }
```

| Aspecto | `PedidoController` (API) | `PedidoViewController` (vistas) |
|---------|--------------------------|---------------------------------|
| Anotación | `@RestController` | `@Controller` |
| Ruta base | `/api/pedidos` | `/pedidos` |
| Devuelve | JSON | Nombre de plantilla o `redirect:` |
| Entrada | `@RequestBody` (JSON) | `@ModelAttribute` (formulario) |
| Errores de validación | `GlobalExceptionHandler` | `VistaExceptionHandler` |

El controlador **no contiene lógica de negocio ni `try/catch`**. Solo recibe la petición, llama al servicio y decide qué vista mostrar. Los errores los atienden los manejadores (ver [`EXCEPTIONS.md`](EXCEPTIONS.md)).

---

# Rutas

| Método | Ruta | Método Java | Resultado |
|--------|------|-------------|-----------|
| `GET` | `/pedidos` | `listar` | `pedidos/lista` |
| `GET` | `/pedidos/nuevo` | `formularioNuevo` | `pedidos/formulario` |
| `POST` | `/pedidos` | `guardar` | `redirect:/pedidos` |
| `GET` | `/pedidos/{id}/editar` | `formularioEditar` | `pedidos/formulario` |
| `POST` | `/pedidos/{id}` | `actualizar` | `redirect:/pedidos` |
| `POST` | `/pedidos/{id}/entregar` | `marcarEntregado` | `redirect:/pedidos` |
| `POST` | `/pedidos/{id}/eliminar` | `eliminar` | `redirect:/pedidos` |

Se usa `POST` para actualizar, entregar y eliminar porque los formularios HTML solo soportan `GET` y `POST`.

---

# Métodos

## Listar pedidos

```java
@GetMapping
public String listar(Model model) {
    model.addAttribute("pedidos", servicio.listarTodos());
    return "pedidos/lista";
}
```

Carga la lista de `PedidoDTO` en el `Model` con el nombre `pedidos`. La plantilla `lista.html` la recorre para armar la tabla.

## Formulario para crear

```java
@GetMapping("/nuevo")
public String formularioNuevo(Model model) {
    model.addAttribute("nuevoPedido", new CrearPedidoDTO("", "", null));
    model.addAttribute("esEdicion", false);
    return "pedidos/formulario";
}
```

Envía un DTO vacío con el nombre `nuevoPedido` para que el formulario tenga un objeto al cual enlazar sus campos. `esEdicion = false` indica a la plantilla que está en modo creación.

## Guardar un pedido nuevo

```java
@PostMapping
public String guardar(@Valid @ModelAttribute("nuevoPedido") CrearPedidoDTO dto) {
    servicio.crear(dto);
    return "redirect:/pedidos";
}
```

* `@ModelAttribute("nuevoPedido")` enlaza los campos del formulario con el DTO.
* `@Valid` ejecuta las validaciones del DTO.
* Si **fallan**, el método no se ejecuta: actúa `VistaExceptionHandler`, que vuelve a mostrar el formulario con los errores.
* Si **pasan**, se crea el pedido y se redirige a la lista.

## Formulario para editar

```java
@GetMapping("/{id}/editar")
public String formularioEditar(@PathVariable Long id, Model model) {
    PedidoDTO pedido = servicio.obtenerPorId(id);

    model.addAttribute("nuevoPedido",
            new CrearPedidoDTO(pedido.cliente(), pedido.plato(), pedido.precio()));
    model.addAttribute("idPedido", id);
    model.addAttribute("esEdicion", true);
    return "pedidos/formulario";
}
```

Busca el pedido y precarga el formulario con sus datos. Envía también `idPedido` (para que el formulario sepa a qué URL enviarse) y `esEdicion = true`.

## Guardar la edición

```java
@PostMapping("/{id}")
public String actualizar(@PathVariable Long id,
        @Valid @ModelAttribute("nuevoPedido") CrearPedidoDTO dto) {
    servicio.actualizar(id, dto);
    return "redirect:/pedidos";
}
```

Igual que `guardar`, pero llama a `servicio.actualizar(id, dto)`. Si la validación falla, `VistaExceptionHandler` detecta el `{id}` en la URL y muestra el formulario en modo edición.

## Marcar como entregado

```java
@PostMapping("/{id}/entregar")
public String marcarEntregado(@PathVariable Long id) {
    servicio.marcarEntregado(id);
    return "redirect:/pedidos";
}
```

## Eliminar

```java
@PostMapping("/{id}/eliminar")
public String eliminar(@PathVariable Long id) {
    servicio.eliminar(id);
    return "redirect:/pedidos";
}
```

---

# El nombre `nuevoPedido`

Tanto el formulario de creación como el de edición usan el mismo atributo: **`nuevoPedido`**. Es el nombre que une tres piezas:

```text
@ModelAttribute("nuevoPedido")   en el controlador
model.addAttribute("nuevoPedido") al mostrar el formulario
th:object="${nuevoPedido}"        en formulario.html
```

Si estos nombres no coinciden, Thymeleaf no puede enlazar los campos ni mostrar los errores.

---

# Patrón Post/Redirect/Get

Después de cada acción que modifica datos (`guardar`, `actualizar`, `marcarEntregado`, `eliminar`) el controlador **no devuelve una página**, sino una redirección:

```text
POST /pedidos            →  crea el pedido
        ↓
redirect:/pedidos        →  el navegador hace GET /pedidos
        ↓
Se muestra la lista actualizada
```

Esto evita que, al recargar la página (F5), el navegador reenvíe el formulario y se cree el pedido por duplicado.

---

# Flujo completo de un formulario

```text
GET /pedidos/nuevo
   ↓
Se muestra el formulario vacío
   ↓
El usuario envía los datos  →  POST /pedidos
   ↓
┌───────────────────────────┐
│ ¿Datos válidos? (@Valid)  │
└──────┬─────────────┬──────┘
      sí            no
       ↓             ↓
servicio.crear()   VistaExceptionHandler
       ↓             ↓
redirect:/pedidos  formulario con errores
```

---

# Plantillas relacionadas

| Plantilla | Usada por |
|-----------|-----------|
| `templates/pedidos/lista.html` | `listar` |
| `templates/pedidos/formulario.html` | `formularioNuevo`, `formularioEditar` y `VistaExceptionHandler` |
| `templates/fragmentos/comunes.html` | Fragmentos compartidos por las plantillas |
| `templates/index.html` | Página de inicio (`/`) |
