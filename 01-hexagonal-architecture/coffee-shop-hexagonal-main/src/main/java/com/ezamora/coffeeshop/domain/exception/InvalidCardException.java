package com.ezamora.coffeeshop.domain.exception;

/** Error de dominio (sin dependencias de framework). */
public class InvalidCardException extends RuntimeException {

    public InvalidCardException(String message) {
        super(message);
    }
}
