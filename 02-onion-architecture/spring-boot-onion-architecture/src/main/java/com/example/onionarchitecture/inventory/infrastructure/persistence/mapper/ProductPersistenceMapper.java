package com.example.onionarchitecture.inventory.infrastructure.persistence.mapper;

import com.example.onionarchitecture.inventory.domain.model.Product;
import com.example.onionarchitecture.inventory.domain.model.ProductId;
import com.example.onionarchitecture.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductPersistenceMapper {

    public ProductJpaEntity toJpaEntity(Product product) {
        UUID id = product.getId() != null ? product.getId().value() : UUID.randomUUID();
        return new ProductJpaEntity(id, product.getSku(), product.getName(), product.isDiscontinued());
    }

    public Product toDomain(ProductJpaEntity entity) {
        return new Product(new ProductId(entity.getId()), entity.getSku(), entity.getName(), entity.isDiscontinued());
    }
}
