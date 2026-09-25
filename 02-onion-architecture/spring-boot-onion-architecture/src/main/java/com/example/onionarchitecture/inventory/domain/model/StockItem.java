package com.example.onionarchitecture.inventory.domain.model;

public class StockItem {

    private final ProductId productId;
    private int quantityOnHand;

    public StockItem(ProductId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        this.productId = productId;
        this.quantityOnHand = 0;
    }

    public StockItem(ProductId productId, int quantityOnHand) {
        this.productId = productId;
        this.quantityOnHand = quantityOnHand;
    }

    public ProductId getProductId() {
        return productId;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public void receive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.quantityOnHand += quantity;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (quantity > quantityOnHand) {
            throw new InsufficientStockException(
                    "Not enough stock: requested " + quantity + ", available " + quantityOnHand);
        }
        this.quantityOnHand -= quantity;
    }
}
