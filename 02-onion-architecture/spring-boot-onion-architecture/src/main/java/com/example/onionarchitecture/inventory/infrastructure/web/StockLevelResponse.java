package com.example.onionarchitecture.inventory.infrastructure.web;

import com.example.onionarchitecture.inventory.application.dto.StockLevel;

public record StockLevelResponse(ProductResponse product, int quantityOnHand) {

    public static StockLevelResponse from(StockLevel stockLevel) {
        return new StockLevelResponse(ProductResponse.from(stockLevel.product()), stockLevel.quantityOnHand());
    }
}
