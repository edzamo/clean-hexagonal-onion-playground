package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderJpaEntity;

public interface OrderRepository extends JpaRepository<OrderJpaEntity, Long> {
    Optional<OrderJpaEntity> findByUuid(UUID uuid);
}
