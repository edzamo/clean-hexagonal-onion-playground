package com.example.onionarchitecture.inventory.application.service;

import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.ProductNotFoundException;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.domain.repository.ProductRepository;
import com.example.onionarchitecture.inventory.domain.repository.StockRepository;
import com.example.onionarchitecture.inventory.domain.service.InventoryDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DiscontinueProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final InventoryDomainService inventoryDomainService;

    @Transactional
    public Product discontinue(ProductId productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        StockItem stockItem = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        inventoryDomainService.discontinueProduct(product, stockItem);

        return productRepository.save(product);
    }
}
