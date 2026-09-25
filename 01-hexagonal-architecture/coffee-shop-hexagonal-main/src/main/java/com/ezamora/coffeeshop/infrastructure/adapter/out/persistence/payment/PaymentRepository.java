package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {

    Optional<PaymentJpaEntity> findByOrderUuid(UUID orderUuid);
}
