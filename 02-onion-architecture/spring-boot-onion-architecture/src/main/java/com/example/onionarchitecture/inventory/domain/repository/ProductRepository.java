package com.example.onionarchitecture.inventory.domain.repository;

import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductId;

import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(ProductId productId);

    Product save(Product product);
}
