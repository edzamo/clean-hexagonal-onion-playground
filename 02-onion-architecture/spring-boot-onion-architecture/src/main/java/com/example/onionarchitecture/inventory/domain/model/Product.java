package com.example.onionarchitecture.inventory.domain.model;

public class Product {

    private ProductId id;
    private final String sku;
    private final String name;
    private boolean discontinued;

    public Product(String sku, String name) {
        if (sku == null || sku.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("sku and name are required");
        }
        this.sku = sku;
        this.name = name;
        this.discontinued = false;
    }

    public Product(ProductId id, String sku, String name, boolean discontinued) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.discontinued = discontinued;
    }

    public ProductId getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public boolean isDiscontinued() {
        return discontinued;
    }

    public void discontinue() {
        this.discontinued = true;
    }
}
