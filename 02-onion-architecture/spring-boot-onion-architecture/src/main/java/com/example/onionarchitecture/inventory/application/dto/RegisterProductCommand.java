package com.example.onionarchitecture.inventory.application.dto;

public record RegisterProductCommand(String sku, String name) {

    public RegisterProductCommand {
        if (sku == null || sku.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("sku and name are required");
        }
    }
}
