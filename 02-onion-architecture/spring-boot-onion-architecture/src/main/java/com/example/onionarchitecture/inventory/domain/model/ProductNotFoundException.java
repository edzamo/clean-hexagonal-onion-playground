package com.example.onionarchitecture.inventory.domain.model;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(ProductId productId) {
        super("Product not found: " + productId.value());
    }
}
