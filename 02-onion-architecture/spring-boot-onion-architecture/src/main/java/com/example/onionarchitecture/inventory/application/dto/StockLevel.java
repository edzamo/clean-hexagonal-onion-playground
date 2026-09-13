package com.example.onionarchitecture.inventory.application.dto;

import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.StockItem;

public record StockLevel(Product product, int quantityOnHand) {

    public static StockLevel of(Product product, StockItem stockItem) {
        return new StockLevel(product, stockItem.getQuantityOnHand());
    }
}
