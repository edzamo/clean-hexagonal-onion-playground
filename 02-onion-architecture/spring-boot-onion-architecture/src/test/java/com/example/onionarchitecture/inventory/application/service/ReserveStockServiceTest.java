package com.example.onionarchitecture.inventory.application.service;

import com.example.onionarchitecture.inventory.application.dto.ReserveStockCommand;
import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.ProductNotFoundException;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.domain.repository.ProductRepository;
import com.example.onionarchitecture.inventory.domain.repository.StockRepository;
import com.example.onionarchitecture.inventory.domain.service.InventoryDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReserveStockServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    // Real domain service, not mocked — it's pure domain logic, no reason to fake it.
    private final InventoryDomainService inventoryDomainService = new InventoryDomainService();

    @Test
    void reservesStockThroughTheDomainService() {
        ProductId id = ProductId.newId();
        when(productRepository.findById(id)).thenReturn(Optional.of(new Product(id, "SKU-1", "Test", false)));
        when(stockRepository.findByProductId(id)).thenReturn(Optional.of(new StockItem(id, 50)));
        when(stockRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReserveStockService service = new ReserveStockService(productRepository, stockRepository, inventoryDomainService);

        StockItem result = service.reserve(new ReserveStockCommand(id, 20));

        assertThat(result.getQuantityOnHand()).isEqualTo(30);
    }

    @Test
    void failsWithNotFoundWhenProductDoesNotExist() {
        ProductId id = ProductId.newId();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        ReserveStockService service = new ReserveStockService(productRepository, stockRepository, inventoryDomainService);

        assertThatThrownBy(() -> service.reserve(new ReserveStockCommand(id, 20)))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
