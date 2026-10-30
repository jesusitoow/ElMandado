package com.elmandado.exepciones;

public class ValidacionException extends RuntimeException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}