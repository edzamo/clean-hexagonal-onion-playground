package com.example.cleanarchitecture.bank.interfaceadapters.gateway;

import com.example.cleanarchitecture.bank.interfaceadapters.gateway.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
}
