package com.example.onionarchitecture.inventory.domain.service;

import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductDiscontinuedException;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * No Spring, no mocks, no repositories — just the pure domain policy that
 * spans two aggregates. This is the whole point of a Domain Service: it's
 * testable exactly like any other piece of domain logic.
 */
class InventoryDomainServiceTest {

    private final InventoryDomainService domainService = new InventoryDomainService();

    private Product product(ProductId id, boolean discontinued) {
        return new Product(id, "SKU-1", "Test product", discontinued);
    }

    @Test
    void reservesStockWhenProductIsActive() {
        ProductId id = ProductId.newId();
        Product product = product(id, false);
        StockItem stockItem = new StockItem(id, 50);

        domainService.reserveStock(product, stockItem, 20);

        assertThat(stockItem.getQuantityOnHand()).isEqualTo(30);
    }

    @Test
    void failsToReserveStockForADiscontinuedProduct() {
        ProductId id = ProductId.newId();
        Product product = product(id, true);
        StockItem stockItem = new StockItem(id, 50);

        assertThatThrownBy(() -> domainService.reserveStock(product, stockItem, 20))
                .isInstanceOf(ProductDiscontinuedException.class);

        // The rule fires before touching the stock — quantity is untouched.
        assertThat(stockItem.getQuantityOnHand()).isEqualTo(50);
    }

    @Test
    void discontinuesProductWithNoStockOnHand() {
        ProductId id = ProductId.newId();
        Product product = product(id, false);
        StockItem stockItem = new StockItem(id, 0);

        domainService.discontinueProduct(product, stockItem);

        assertThat(product.isDiscontinued()).isTrue();
    }

    @Test
    void failsToDiscontinueProductWithStockStillOnHand() {
        ProductId id = ProductId.newId();
        Product product = product(id, false);
        StockItem stockItem = new StockItem(id, 5);

        assertThatThrownBy(() -> domainService.discontinueProduct(product, stockItem))
                .isInstanceOf(IllegalStateException.class);
        assertThat(product.isDiscontinued()).isFalse();
    }
}
