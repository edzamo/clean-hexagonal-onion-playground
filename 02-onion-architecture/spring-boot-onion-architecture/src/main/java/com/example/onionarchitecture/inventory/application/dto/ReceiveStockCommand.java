package com.example.onionarchitecture.inventory.application.dto;

import com.example.onionarchitecture.inventory.domain.model.ProductId;

public record ReceiveStockCommand(ProductId productId, int quantity) {

    public ReceiveStockCommand {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
    }
}
