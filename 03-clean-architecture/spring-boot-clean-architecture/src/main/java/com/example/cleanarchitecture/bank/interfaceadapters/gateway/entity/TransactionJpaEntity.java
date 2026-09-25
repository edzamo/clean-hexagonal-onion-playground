package com.example.cleanarchitecture.bank.interfaceadapters.gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "related_account_id")
    private UUID relatedAccountId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected TransactionJpaEntity() {
    }

    public TransactionJpaEntity(UUID id, UUID accountId, String type, BigDecimal amount,
                                 UUID relatedAccountId, Instant occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.relatedAccountId = relatedAccountId;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getRelatedAccountId() {
        return relatedAccountId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
