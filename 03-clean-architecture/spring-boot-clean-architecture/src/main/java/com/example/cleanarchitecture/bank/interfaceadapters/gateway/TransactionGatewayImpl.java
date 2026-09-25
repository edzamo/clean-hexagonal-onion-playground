package com.example.cleanarchitecture.bank.interfaceadapters.gateway;

import com.example.cleanarchitecture.bank.entities.Transaction;
import com.example.cleanarchitecture.bank.interfaceadapters.gateway.mapper.TransactionPersistenceMapper;
import com.example.cleanarchitecture.bank.usecases.port.out.TransactionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TransactionGatewayImpl implements TransactionGateway {

    private final SpringDataTransactionRepository repository;
    private final TransactionPersistenceMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(repository.save(mapper.toJpaEntity(transaction)));
    }
}
