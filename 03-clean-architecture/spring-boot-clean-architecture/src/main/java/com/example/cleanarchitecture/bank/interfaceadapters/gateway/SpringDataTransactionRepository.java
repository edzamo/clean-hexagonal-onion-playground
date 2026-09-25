package com.example.cleanarchitecture.bank.interfaceadapters.gateway;

import com.example.cleanarchitecture.bank.interfaceadapters.gateway.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {
}
