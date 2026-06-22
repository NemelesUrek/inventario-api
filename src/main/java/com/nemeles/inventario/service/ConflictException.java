package com.nemeles.inventario.service;

/** Conflicto de estado (SKU duplicado, stock insuficiente) -> HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
