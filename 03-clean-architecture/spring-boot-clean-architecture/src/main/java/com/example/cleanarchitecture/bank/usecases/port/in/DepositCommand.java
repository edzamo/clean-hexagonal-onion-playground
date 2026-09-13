package com.example.cleanarchitecture.bank.usecases.port.in;

import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.Money;

public record DepositCommand(AccountId accountId, Money amount) {

    public DepositCommand {
        if (accountId == null || amount == null) {
            throw new IllegalArgumentException("accountId and amount are required");
        }
    }
}
