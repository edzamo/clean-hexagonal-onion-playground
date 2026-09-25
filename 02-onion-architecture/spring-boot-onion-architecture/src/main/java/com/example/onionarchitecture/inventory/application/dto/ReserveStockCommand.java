package com.example.onionarchitecture.inventory.application.dto;

import com.example.onionarchitecture.inventory.domain.model.ProductId;

public record ReserveStockCommand(ProductId productId, int quantity) {

    public ReserveStockCommand {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
    }
}
