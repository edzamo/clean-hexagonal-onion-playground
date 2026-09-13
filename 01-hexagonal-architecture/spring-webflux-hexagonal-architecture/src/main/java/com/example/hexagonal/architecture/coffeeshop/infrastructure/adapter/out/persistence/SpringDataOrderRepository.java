package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence;

import com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface SpringDataOrderRepository extends ReactiveCrudRepository<OrderEntity, UUID> {
}
