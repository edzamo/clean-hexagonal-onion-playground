package com.ezamora.coffeeshop.domain.model.exception;

/** Error de dominio (sin dependencias de framework). */
public class OrderStateException extends RuntimeException {

    public OrderStateException(String message) {
        super(message);
    }
}
