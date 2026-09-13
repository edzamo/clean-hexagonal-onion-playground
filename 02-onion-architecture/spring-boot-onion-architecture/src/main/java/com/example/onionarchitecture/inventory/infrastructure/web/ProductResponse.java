package com.example.onionarchitecture.inventory.infrastructure.web;

import com.example.onionarchitecture.inventory.domain.model.Product;

import java.util.UUID;

public record ProductResponse(UUID id, String sku, String name, boolean discontinued) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId().value(), product.getSku(), product.getName(), product.isDiscontinued());
    }
}
