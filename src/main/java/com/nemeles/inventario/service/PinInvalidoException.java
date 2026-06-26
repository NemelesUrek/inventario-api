package com.nemeles.inventario.service;

/** El PIN de confirmación no coincide. Se mapea a HTTP 422 (no 401/403). */
public class PinInvalidoException extends RuntimeException {
    public PinInvalidoException(String mensaje) {
        super(mensaje);
    }
}
