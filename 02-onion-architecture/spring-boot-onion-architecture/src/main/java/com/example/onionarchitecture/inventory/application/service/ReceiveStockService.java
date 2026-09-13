package com.example.onionarchitecture.inventory.application.service;

import com.example.onionarchitecture.inventory.application.dto.ReceiveStockCommand;
import com.example.onionarchitecture.inventory.domain.model.ProductNotFoundException;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.domain.repository.ProductRepository;
import com.example.onionarchitecture.inventory.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A single-aggregate operation (only {@code StockItem} changes) — no need
 * for the {@code InventoryDomainService} here, unlike reserve/discontinue.
 */
@RequiredArgsConstructor
@Service
public class ReceiveStockService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Transactional
    public StockItem receive(ReceiveStockCommand command) {
        productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        StockItem stockItem = stockRepository.findByProductId(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        stockItem.receive(command.quantity());
        return stockRepository.save(stockItem);
    }
}
