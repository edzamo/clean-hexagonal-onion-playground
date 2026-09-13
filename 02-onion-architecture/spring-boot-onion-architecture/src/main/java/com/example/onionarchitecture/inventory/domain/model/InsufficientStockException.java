package com.example.onionarchitecture.inventory.domain.model;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
