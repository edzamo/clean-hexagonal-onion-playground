package com.example.onionarchitecture.inventory.domain.service;

import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductDiscontinuedException;
import com.example.onionarchitecture.inventory.domain.model.StockItem;

/**
 * A Domain Service — the concept that sets Onion Architecture apart from
 * Hexagonal/Clean in this playground. It holds business rules that don't fit
 * naturally inside a single aggregate (here: {@code Product} and
 * {@code StockItem} are two separate aggregates), but are still pure domain
 * policy — no repositories, no I/O, no framework, no persistence concerns.
 * Loading/saving those aggregates and wrapping this in a transaction is the
 * Application Service's job, not this class's.
 */
public class InventoryDomainService {

    public void reserveStock(Product product, StockItem stockItem, int quantity) {
        if (product.isDiscontinued()) {
            throw new ProductDiscontinuedException(
                    "Cannot reserve stock for discontinued product " + product.getSku());
        }
        stockItem.reserve(quantity);
    }

    public void discontinueProduct(Product product, StockItem stockItem) {
        if (stockItem.getQuantityOnHand() > 0) {
            throw new IllegalStateException(
                    "Cannot discontinue product " + product.getSku() + " with stock still on hand");
        }
        product.discontinue();
    }
}
