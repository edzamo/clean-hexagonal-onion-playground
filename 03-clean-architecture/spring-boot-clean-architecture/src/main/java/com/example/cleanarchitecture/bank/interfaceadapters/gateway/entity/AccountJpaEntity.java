package com.example.cleanarchitecture.bank.interfaceadapters.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A real JPA {@code @Entity} — mapped to a table row. Distinct from the
 * domain {@code Account} (entities layer), which never imports
 * {@code jakarta.persistence}. Same rule as in the hexagonal playground:
 * "Entity" in JPA (ORM mapping) is not "Entity" in DDD (identity + lifecycle).
 */
@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private String status;

    protected AccountJpaEntity() {
        // required by JPA/Hibernate to build a managed proxy
    }

    public AccountJpaEntity(UUID id, String holderName, BigDecimal balance, String status) {
        this.id = id;
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getHolderName() {
        return holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }
}
