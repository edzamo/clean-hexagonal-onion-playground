package com.example.onionarchitecture.inventory.infrastructure.persistence;

import com.example.onionarchitecture.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {
}
