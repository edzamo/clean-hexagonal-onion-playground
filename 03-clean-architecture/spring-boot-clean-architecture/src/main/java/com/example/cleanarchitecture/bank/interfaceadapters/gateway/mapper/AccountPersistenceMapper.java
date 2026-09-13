package com.example.cleanarchitecture.bank.interfaceadapters.gateway.mapper;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.AccountStatus;
import com.example.cleanarchitecture.bank.entities.Money;
import com.example.cleanarchitecture.bank.interfaceadapters.gateway.entity.AccountJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountPersistenceMapper {

    public AccountJpaEntity toJpaEntity(Account account) {
        UUID id = account.getId() != null ? account.getId().value() : UUID.randomUUID();
        return new AccountJpaEntity(id, account.getHolderName(), account.getBalance().amount(), account.getStatus().name());
    }

    public Account toDomain(AccountJpaEntity entity) {
        return new Account(
                new AccountId(entity.getId()),
                entity.getHolderName(),
                new Money(entity.getBalance()),
                AccountStatus.valueOf(entity.getStatus()));
    }
}
