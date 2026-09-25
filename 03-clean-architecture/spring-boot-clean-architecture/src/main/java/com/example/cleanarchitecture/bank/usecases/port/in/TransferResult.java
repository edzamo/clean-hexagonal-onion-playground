package com.example.cleanarchitecture.bank.usecases.port.in;

import com.example.cleanarchitecture.bank.entities.Account;

public record TransferResult(Account sourceAccount, Account targetAccount) {
}
