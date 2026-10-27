package com.elmandado.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cliente;
    private String plato;
    private Double precio;
    private boolean entregado;

    public Pedido() {
    }

    public Pedido(String cliente, String plato, Double precio) {
        this.cliente = cliente;
        this.plato = plato;
        this.precio = precio;
        this.entregado = false;
    }

    public Pedido(Long id, String cliente, String plato, Double precio) {
        this.id = id;
        this.cliente = cliente;
        this.plato = plato;
        this.precio = precio;
        this.entregado = false;
    }
}