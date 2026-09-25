package com.example.cleanarchitecture.bank.usecases.port.out;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountId;

import java.util.Optional;

public interface AccountGateway {

    Optional<Account> findById(AccountId accountId);

    Account save(Account account);
}
