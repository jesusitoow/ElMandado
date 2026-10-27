# Repositorio - PedidoRepository

## Descripción

`PedidoRepository` es la capa de acceso a datos. Antes los pedidos se guardaban en memoria (`PedidoRepositoryMemoria`, con `ConcurrentHashMap` y `AtomicLong`); ahora se guardan en una **base de datos** con **Spring Data JPA**.

```java
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    default List<Pedido> buscarTodos() {
        return findAll();
    }

    default Optional<Pedido> buscarPorId(Long id) {
        return findById(id);
    }

    default Pedido guardar(Pedido pedido) {
        return save(pedido);
    }

    default void eliminar(Long id) {
        deleteById(id);
    }

    default boolean existePorId(Long id) {
        return existsById(id);
    }
}
```

---

# ¿Cómo funciona?

## `JpaRepository<Pedido, Long>`

Al extender `JpaRepository`, el repositorio hereda las operaciones CRUD listas para usar. Los dos parámetros indican:

| Parámetro | Significado |
|-----------|-------------|
| `Pedido` | La entidad que administra |
| `Long` | El tipo de su clave primaria (`id`) |

**No hay clase de implementación.** Spring Data genera una en tiempo de ejecución. Por eso `PedidoRepositoryMemoria` ya no existe.

## Métodos `default`

El servicio ya usaba nombres en español (`buscarTodos`, `buscarPorId`, `guardar`, `eliminar`, `existePorId`). En lugar de cambiar el servicio, la interfaz define métodos `default` que delegan en los métodos heredados de JPA:

| Método del servicio | Método de JPA al que delega |
|---------------------|-----------------------------|
| `buscarTodos()` | `findAll()` |
| `buscarPorId(id)` | `findById(id)` |
| `guardar(pedido)` | `save(pedido)` |
| `eliminar(id)` | `deleteById(id)` |
| `existePorId(id)` | `existsById(id)` |

Así la migración de memoria a JPA **no obligó a modificar `PedidoServiceImpl`**, y el servicio sigue sin depender de nombres propios de Spring Data.

---

# Comparación con la versión anterior

| Aspecto | Antes | Ahora |
|---------|-------|-------|
| Almacenamiento | En memoria | Base de datos |
| Persistencia al reiniciar | Se pierde todo | Se conserva |
| Generación del ID | `AtomicLong` | La base de datos (según la estrategia de la entidad) |
| Implementación | Clase `PedidoRepositoryMemoria` | Generada por Spring Data |
| Interfaz | Propia | Extiende `JpaRepository` |

---

# Requisitos

* Dependencia **Spring Data JPA** en el `pom.xml`.
* Dependencia del **driver** del motor de base de datos elegido.
* Conexión configurada en `application.properties` (`spring.datasource.url`, `username`, `password`).
* La entidad `Pedido` debe estar anotada como entidad JPA (`@Entity`, `@Id`, etc.).

---

# Comunicación con el servicio

```text
PedidoServiceImpl
      ↓  buscarPorId(), guardar(), existePorId()...
PedidoRepository
      ↓  findById(), save(), existsById()...
Spring Data JPA (implementación generada)
      ↓
Base de datos
```

El repositorio trabaja con la entidad `Pedido` y no conoce los DTOs ni los controladores. Las transacciones las controla el servicio con `@Transactional` (ver [`SERVICE.md`](SERVICE.md)).
