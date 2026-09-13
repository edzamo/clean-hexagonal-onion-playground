package com.example.onionarchitecture.inventory.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "stock_items")
public class StockJpaEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    protected StockJpaEntity() {
    }

    public StockJpaEntity(UUID productId, int quantityOnHand) {
        this.productId = productId;
        this.quantityOnHand = quantityOnHand;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }
}
