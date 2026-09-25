package com.ezamora.coffeeshop.domain.exception;

/** Error de dominio (sin dependencias de framework). */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
