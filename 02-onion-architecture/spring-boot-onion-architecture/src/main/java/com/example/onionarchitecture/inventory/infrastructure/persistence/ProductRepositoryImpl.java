package com.example.onionarchitecture.inventory.infrastructure.persistence;

import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.domain.repository.ProductRepository;
import com.example.onionarchitecture.inventory.infrastructure.persistence.mapper.ProductPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringDataProductRepository repository;
    private final ProductPersistenceMapper mapper;

    @Override
    public Optional<Product> findById(ProductId productId) {
        return repository.findById(productId.value()).map(mapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(product)));
    }
}
