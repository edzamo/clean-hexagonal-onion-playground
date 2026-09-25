package com.example.cleanarchitecture.bank.entities;

import java.time.Instant;

/**
 * A ledger record: created once, never mutated afterwards — an immutable
 * historical fact, not a mutable Entity, even though it carries an id.
 */
public record Transaction(
        TransactionId id,
        AccountId accountId,
        TransactionType type,
        Money amount,
        AccountId relatedAccountId,
        Instant occurredAt) {

    public static Transaction occur(AccountId accountId, TransactionType type, Money amount, AccountId relatedAccountId) {
        return new Transaction(TransactionId.newId(), accountId, type, amount, relatedAccountId, Instant.now());
    }
}
