package com.example.onionarchitecture.inventory.application.service;

import com.example.onionarchitecture.inventory.application.dto.StockLevel;
import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.ProductNotFoundException;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.domain.repository.ProductRepository;
import com.example.onionarchitecture.inventory.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GetStockLevelService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public StockLevel getStockLevel(ProductId productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        StockItem stockItem = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return StockLevel.of(product, stockItem);
    }
}
