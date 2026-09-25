package com.example.cleanarchitecture.bank.interfaceadapters.gateway.mapper;

import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.Money;
import com.example.cleanarchitecture.bank.entities.Transaction;
import com.example.cleanarchitecture.bank.entities.TransactionId;
import com.example.cleanarchitecture.bank.entities.TransactionType;
import com.example.cleanarchitecture.bank.interfaceadapters.gateway.entity.TransactionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    public TransactionJpaEntity toJpaEntity(Transaction transaction) {
        return new TransactionJpaEntity(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.type().name(),
                transaction.amount().amount(),
                transaction.relatedAccountId() != null ? transaction.relatedAccountId().value() : null,
                transaction.occurredAt());
    }

    public Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(
                new TransactionId(entity.getId()),
                new AccountId(entity.getAccountId()),
                TransactionType.valueOf(entity.getType()),
                new Money(entity.getAmount()),
                entity.getRelatedAccountId() != null ? new AccountId(entity.getRelatedAccountId()) : null,
                entity.getOccurredAt());
    }
}
