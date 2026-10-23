package com.elmandado.dto;

public record PedidoDTO(
        Long id,
        String cliente,
        String plato,
        Double precio,
        boolean entregado) {
}