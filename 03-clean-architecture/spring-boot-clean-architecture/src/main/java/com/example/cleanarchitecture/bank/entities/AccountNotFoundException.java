package com.example.cleanarchitecture.bank.entities;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(AccountId accountId) {
        super("Account not found: " + accountId.value());
    }
}
