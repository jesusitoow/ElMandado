package com.elmandado.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elmandado.dto.CrearPedidoDTO;
import com.elmandado.dto.PedidoDTO;
import com.elmandado.exepciones.RecursoNoEncontradoException;
import com.elmandado.exepciones.ValidacionException;
import com.elmandado.modelo.Pedido;
import com.elmandado.repositorio.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repositorio;

    @Override
    public List<PedidoDTO> listarTodos() {
        return repositorio.buscarTodos().stream()
                .map(this::aDTO)
                .toList();
    }

    @Override
    public PedidoDTO obtenerPorId(Long id) {
        return aDTO(buscarOFallar(id));
    }

    @Override
    @Transactional
    public PedidoDTO crear(CrearPedidoDTO dto) {
        if (dto == null) {
            throw new ValidacionException("Los datos del pedido son obligatorios");
        }

        Pedido pedido = new Pedido(dto.cliente(), dto.plato(), dto.precio());
        return aDTO(repositorio.guardar(pedido));
    }

    @Override
    @Transactional
    public PedidoDTO actualizar(Long id, CrearPedidoDTO dto) {
        Pedido pedido = buscarOFallar(id);

        pedido.setCliente(dto.cliente());
        pedido.setPlato(dto.plato());
        pedido.setPrecio(dto.precio());

        return aDTO(repositorio.guardar(pedido));
    }

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

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repositorio.existePorId(id)) {
            throw new RecursoNoEncontradoException("No existe el pedido con ID: " + id);
        }
        repositorio.eliminar(id);
    }

    private Pedido buscarOFallar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con ID: " + id));
    }

    private PedidoDTO aDTO(Pedido p) {
        return new PedidoDTO(p.getId(), p.getCliente(), p.getPlato(), p.getPrecio(), p.isEntregado());
    }
}