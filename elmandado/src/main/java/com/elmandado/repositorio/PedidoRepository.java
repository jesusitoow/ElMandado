package com.elmandado.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.elmandado.modelo.Pedido;

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