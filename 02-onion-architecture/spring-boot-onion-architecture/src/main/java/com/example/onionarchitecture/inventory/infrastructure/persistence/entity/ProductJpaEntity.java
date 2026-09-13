package com.example.onionarchitecture.inventory.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean discontinued;

    protected ProductJpaEntity() {
    }

    public ProductJpaEntity(UUID id, String sku, String name, boolean discontinued) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.discontinued = discontinued;
    }

    public UUID getId() {
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
}
