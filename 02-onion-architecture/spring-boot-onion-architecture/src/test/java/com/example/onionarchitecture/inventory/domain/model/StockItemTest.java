package com.example.onionarchitecture.inventory.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockItemTest {

    @Test
    void newStockItemStartsAtZero() {
        StockItem stockItem = new StockItem(ProductId.newId());

        assertThat(stockItem.getQuantityOnHand()).isEqualTo(0);
    }

    @Test
    void receiveIncreasesQuantity() {
        StockItem stockItem = new StockItem(ProductId.newId());

        stockItem.receive(50);

        assertThat(stockItem.getQuantityOnHand()).isEqualTo(50);
    }

    @Test
    void reserveDecreasesQuantity() {
        StockItem stockItem = new StockItem(ProductId.newId());
        stockItem.receive(50);

        stockItem.reserve(20);

        assertThat(stockItem.getQuantityOnHand()).isEqualTo(30);
    }

    @Test
    void reserveFailsWhenNotEnoughStock() {
        StockItem stockItem = new StockItem(ProductId.newId());
        stockItem.receive(10);

        assertThatThrownBy(() -> stockItem.reserve(50))
                .isInstanceOf(InsufficientStockException.class);
    }
}
