package com.example.onionarchitecture.inventory.application.service;

import com.example.onionarchitecture.inventory.application.dto.RegisterProductCommand;
import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.StockItem;
import com.example.onionarchitecture.inventory.domain.repository.ProductRepository;
import com.example.onionarchitecture.inventory.domain.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RegisterProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @Transactional
    public Product register(RegisterProductCommand command) {
        Product saved = productRepository.save(new Product(command.sku(), command.name()));
        stockRepository.save(new StockItem(saved.getId()));
        return saved;
    }
}
