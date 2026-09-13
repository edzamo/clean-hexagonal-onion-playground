package com.example.onionarchitecture.inventory.application.service;

import com.example.onionarchitecture.inventory.application.dto.ReserveStockCommand;
import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductNotFoundException;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.domain.repository.ProductRepository;
import com.example.onionarchitecture.inventory.domain.repository.StockRepository;
import com.example.onionarchitecture.inventory.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spans two aggregates ({@code Product} + {@code StockItem}) — the same
 * shape as {@code TransferInteractor} in the Clean Architecture project.
 * The difference here: the cross-aggregate *rule itself* ("no reservations
 * for a discontinued product") lives in {@link InventoryDomainService}, a
 * pure domain object — this Application Service's job is only to load the
 * aggregates, delegate to that rule, save the result, and own the
 * transaction boundary. In the Clean/Hexagonal projects that same rule
 * would have been written inline inside the Interactor/Service itself,
 * since neither of those architectures names "Domain Service" as a
 * distinct layer.
 */
@RequiredArgsConstructor
@Service
public class ReserveStockService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final InventoryDomainService inventoryDomainService;

    @Transactional
    public StockItem reserve(ReserveStockCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        StockItem stockItem = stockRepository.findByProductId(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        inventoryDomainService.reserveStock(product, stockItem, command.quantity());

        return stockRepository.save(stockItem);
    }
}
