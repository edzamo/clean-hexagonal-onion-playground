package com.example.cleanarchitecture.bank.usecases.port.out;

import com.example.cleanarchitecture.bank.entities.Transaction;

public interface TransactionGateway {

    Transaction save(Transaction transaction);
}
