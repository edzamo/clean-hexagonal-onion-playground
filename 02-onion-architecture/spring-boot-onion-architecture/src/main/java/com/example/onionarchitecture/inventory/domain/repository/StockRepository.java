package com.example.onionarchitecture.inventory.domain.repository;

import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.model.StockItem;

import java.util.Optional;

public interface StockRepository {

    Optional<StockItem> findByProductId(ProductId productId);

    StockItem save(StockItem stockItem);
}
