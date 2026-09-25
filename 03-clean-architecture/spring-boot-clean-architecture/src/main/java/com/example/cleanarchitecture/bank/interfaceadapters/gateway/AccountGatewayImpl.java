package com.example.cleanarchitecture.bank.interfaceadapters.gateway;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.interfaceadapters.gateway.mapper.AccountPersistenceMapper;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AccountGatewayImpl implements AccountGateway {

    private final SpringDataAccountRepository repository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return repository.findById(accountId.value()).map(mapper::toDomain);
    }

    @Override
    public Account save(Account account) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(account)));
    }
}
