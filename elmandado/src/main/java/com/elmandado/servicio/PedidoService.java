package com.elmandado.servicio;

import java.util.List;

import com.elmandado.dto.CrearPedidoDTO;
import com.elmandado.dto.PedidoDTO;

public interface PedidoService {

    List<PedidoDTO> listarTodos();

    PedidoDTO obtenerPorId(Long id);

    PedidoDTO crear(CrearPedidoDTO dto);

    PedidoDTO actualizar(Long id, CrearPedidoDTO dto);

    void marcarEntregado(Long id);

    void eliminar(Long id);
}