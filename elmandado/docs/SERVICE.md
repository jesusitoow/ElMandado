# Servicio de Pedidos

## Descripción

La capa de servicio contiene la lógica de negocio de la aplicación **El Mandado**.

Actúa como intermediario entre los controladores y el repositorio: el controlador recibe la petición, y el servicio decide qué operación realizar, aplica las reglas de negocio y convierte las entidades en DTOs para devolverlas. Lo usan **los dos controladores** (`PedidoController` para la API y `PedidoViewController` para las vistas web).

```text
servicio
│
├── PedidoService
└── PedidoServiceImpl
```

Se apoya en:

```text
dto                      exepciones                       repositorio
├── CrearPedidoDTO       ├── RecursoNoEncontradoException └── PedidoRepository
└── PedidoDTO            └── ValidacionException
```

Documentación relacionada: [`EXCEPTIONS.md`](EXCEPTIONS.md), [`REPOSITORY.md`](REPOSITORY.md), [`VIEW_CONTROLLER.md`](VIEW_CONTROLLER.md).

---

# DTOs

El servicio **no recibe ni devuelve la entidad `Pedido` directamente**. Usa DTOs para separar el modelo interno de lo que se expone.

## CrearPedidoDTO (entrada)

Se usa al crear y al actualizar un pedido. Es un `record` con validaciones de Jakarta Validation:

```java
public record CrearPedidoDTO(
        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 25, message = "El cliente no puede superar los 25 caracteres")
        String cliente,

        @NotBlank(message = "El plato es obligatorio")
        @Size(max = 50, message = "El nombre del plato no puede superar los 50 caracteres")
        String plato,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser positivo")
        Double precio) {
}
```

| Campo | Reglas |
|-------|--------|
| `cliente` | Obligatorio, máximo 25 caracteres |
| `plato` | Obligatorio, máximo 50 caracteres |
| `precio` | Obligatorio, mayor a 0 |

Estas anotaciones se activan cuando el controlador recibe el DTO con `@Valid`.

## PedidoDTO (salida)

```java
public record PedidoDTO(
        Long id,
        String cliente,
        String plato,
        Double precio,
        boolean entregado) {
}
```

---

# PedidoService

Interfaz que define las operaciones disponibles sobre los pedidos:

```java
public interface PedidoService {

    List<PedidoDTO> listarTodos();

    PedidoDTO obtenerPorId(Long id);

    PedidoDTO crear(CrearPedidoDTO dto);

    PedidoDTO actualizar(Long id, CrearPedidoDTO dto);

    void marcarEntregado(Long id);

    void eliminar(Long id);
}
```

---

# PedidoServiceImpl

Implementación de `PedidoService`. Usa tres anotaciones a nivel de clase:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repositorio;
```

| Anotación | Qué hace |
|-----------|----------|
| `@Service` | Registra la clase como bean de Spring. |
| `@RequiredArgsConstructor` | **Lombok** genera el constructor con los campos `final`. Reemplaza el constructor escrito a mano, y Spring inyecta el `PedidoRepository` igual que antes. |
| `@Transactional(readOnly = true)` | Todos los métodos son transacciones de **solo lectura** por defecto. |

Los métodos que modifican datos (`crear`, `actualizar`, `marcarEntregado`, `eliminar`) llevan además `@Transactional`, que **sobrescribe** el `readOnly` de la clase y los vuelve transacciones de escritura.

> Para que `@RequiredArgsConstructor` funcione, Lombok debe estar en el `pom.xml` y el IDE debe tener el plugin de Lombok activo.

Comunicación entre capas:

```text
PedidoController / PedidoViewController
              ↓
PedidoService      (trabaja con DTOs)
              ↓
PedidoRepository   (trabaja con la entidad Pedido)
              ↓
Base de datos (JPA)
```

---

# Métodos auxiliares

Para no repetir código, el servicio tiene dos métodos privados:

```java
private Pedido buscarOFallar(Long id) {
    return repositorio.buscarPorId(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));
}

private PedidoDTO aDTO(Pedido p) {
    return new PedidoDTO(p.getId(), p.getCliente(), p.getPlato(), p.getPrecio(), p.isEntregado());
}
```

| Método | Función |
|--------|---------|
| `buscarOFallar(id)` | Busca el pedido; si no existe lanza `RecursoNoEncontradoException` (→ `404`). |
| `aDTO(pedido)` | Convierte una entidad `Pedido` en un `PedidoDTO`. |

---

# Operaciones del Servicio

## 1. Listar todos los pedidos

```java
@Override
public List<PedidoDTO> listarTodos() {
    return repositorio.buscarTodos().stream()
            .map(this::aDTO)
            .toList();
}
```

```text
PedidoRepository.buscarTodos() → Mapeo Pedido → PedidoDTO → Lista de PedidoDTO
```

## 2. Obtener pedido por ID

```java
@Override
public PedidoDTO obtenerPorId(Long id) {
    return aDTO(buscarOFallar(id));
}
```

Si el pedido no existe se lanza `RecursoNoEncontradoException`.

## 3. Crear un pedido

```java
@Override
@Transactional
public PedidoDTO crear(CrearPedidoDTO dto) {
    if (dto == null) {
        throw new ValidacionException("Los datos del pedido son obligatorios");
    }

    Pedido pedido = new Pedido(dto.cliente(), dto.plato(), dto.precio());
    return aDTO(repositorio.guardar(pedido));
}
```

1. Comprueba que el DTO no sea `null` (si lo es, `ValidacionException`).
2. Crea la entidad `Pedido`. El ID lo asigna la base de datos al guardar.
3. Guarda el pedido y devuelve un `PedidoDTO` con el ID ya asignado.

> Las validaciones de `cliente`, `plato` y `precio` viven en `CrearPedidoDTO` y se ejecutan **antes** de llegar al servicio, cuando el controlador usa `@Valid`.

## 4. Actualizar un pedido

```java
@Override
@Transactional
public PedidoDTO actualizar(Long id, CrearPedidoDTO dto) {
    Pedido pedido = buscarOFallar(id);

    pedido.setCliente(dto.cliente());
    pedido.setPlato(dto.plato());
    pedido.setPrecio(dto.precio());

    return aDTO(repositorio.guardar(pedido));
}
```

1. Busca el pedido (si no existe → `RecursoNoEncontradoException`).
2. Reemplaza `cliente`, `plato` y `precio`.
3. Guarda y devuelve el `PedidoDTO` actualizado.

El estado `entregado` **no se modifica** aquí; eso lo hace `marcarEntregado`.

## 5. Marcar pedido como entregado

```java
@Override
@Transactional
public void marcarEntregado(Long id) {
    Pedido pedido = buscarOFallar(id);

    if (pedido.isEntregado()) {
        throw new ValidacionException("El pedido con ID " + id + " ya había sido marcado como entregado");
    }

    pedido.setEntregado(true);
    repositorio.guardar(pedido);
}
```

1. Busca el pedido (si no existe → `RecursoNoEncontradoException`).
2. Si ya estaba entregado → `ValidacionException`.
3. Cambia `entregado` a `true` y guarda.

## 6. Eliminar pedido

```java
@Override
@Transactional
public void eliminar(Long id) {
    if (!repositorio.existePorId(id)) {
        throw new RecursoNoEncontradoException("No existe el pedido con ID: " + id);
    }
    repositorio.eliminar(id);
}
```

La existencia se comprueba con `existePorId` en lugar de cargar el pedido completo.

---

# Manejo de Validaciones

Las validaciones se reparten en dos niveles:

**1. Validaciones de formato (en el DTO).** Se ejecutan antes de entrar al servicio:

* `cliente`: no vacío, máximo 25 caracteres.
* `plato`: no vacío, máximo 50 caracteres.
* `precio`: no nulo y positivo.

Si fallan, la respuesta depende de quién llamó: en la API, `GlobalExceptionHandler` responde `400` con JSON; en las vistas, `VistaExceptionHandler` vuelve a mostrar el formulario con los errores. Ver [`EXCEPTIONS.md`](EXCEPTIONS.md).

**2. Reglas de negocio (en el servicio).** Dependen del estado del sistema:

* El pedido debe existir → `RecursoNoEncontradoException`.
* Un pedido no puede marcarse como entregado dos veces → `ValidacionException`.
* El DTO de creación no puede ser `null` → `ValidacionException`.

---

# Responsabilidades del Servicio

La capa de servicio se encarga de:

* Aplicar las reglas de negocio.
* Crear, consultar, actualizar y eliminar pedidos.
* Cambiar el estado de entrega.
* Convertir entre la entidad `Pedido` y los DTOs.
* Definir los límites de las transacciones.
* Lanzar excepciones propias cuando algo falla.
* Comunicarse con el repositorio.

La capa de servicio **no** se encarga de:

* Manejar peticiones HTTP ni decidir códigos de estado.
* Validar el formato de los datos de entrada (lo hace el DTO).
* Decidir cómo se muestran los errores (lo hacen los manejadores de excepciones).

---

# Resumen de cambios respecto a la versión anterior

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| Constructor | Escrito a mano | Generado por Lombok (`@RequiredArgsConstructor`) |
| Transacciones | No había | `@Transactional(readOnly = true)` en la clase y `@Transactional` en las escrituras |
| Código repetido | Búsqueda y mapeo en cada método | Métodos `buscarOFallar` y `aDTO` |
| Almacenamiento | `PedidoRepositoryMemoria` | `PedidoRepository` con Spring Data JPA |
| Clientes del servicio | Solo `PedidoController` | `PedidoController` y `PedidoViewController` |

```text
Controller → Service → Repository
```