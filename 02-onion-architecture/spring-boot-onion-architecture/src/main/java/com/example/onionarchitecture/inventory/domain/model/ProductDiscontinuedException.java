package com.example.onionarchitecture.inventory.domain.model;

public class ProductDiscontinuedException extends RuntimeException {

    public ProductDiscontinuedException(String message) {
        super(message);
    }
}
