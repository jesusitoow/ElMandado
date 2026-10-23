package com.elmandado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearPedidoDTO(
        @NotBlank(message = "El cliente es obligatorio") @Size(max = 25, message = "El cliente no puede superar los 25 caracteres") String cliente,

        @NotBlank(message = "El plato es obligatorio") @Size(max = 50, message = "El nombre del plato no puede superar los 50 caracteres") String plato,

        @NotNull(message = "El precio es obligatorio") @Positive(message = "El precio debe ser positivo") Double precio) {
}