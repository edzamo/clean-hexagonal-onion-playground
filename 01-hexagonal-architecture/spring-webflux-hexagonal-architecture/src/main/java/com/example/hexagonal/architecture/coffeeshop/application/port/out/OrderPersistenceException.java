package com.example.hexagonal.architecture.coffeeshop.application.port.out;

public class OrderPersistenceException extends RuntimeException {

    public OrderPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
