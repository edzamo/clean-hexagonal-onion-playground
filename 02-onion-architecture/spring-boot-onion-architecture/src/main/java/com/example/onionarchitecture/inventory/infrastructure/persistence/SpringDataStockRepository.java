package com.example.onionarchitecture.inventory.infrastructure.persistence;

import com.example.onionarchitecture.inventory.infrastructure.persistence.entity.StockJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataStockRepository extends JpaRepository<StockJpaEntity, UUID> {
}
