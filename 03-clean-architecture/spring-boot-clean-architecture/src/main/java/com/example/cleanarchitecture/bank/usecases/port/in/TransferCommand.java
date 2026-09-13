package com.example.cleanarchitecture.bank.usecases.port.in;

import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.Money;

public record TransferCommand(AccountId sourceAccountId, AccountId targetAccountId, Money amount) {

    public TransferCommand {
        if (sourceAccountId == null || targetAccountId == null || amount == null) {
            throw new IllegalArgumentException("sourceAccountId, targetAccountId and amount are required");
        }
        if (sourceAccountId.equals(targetAccountId)) {
            throw new IllegalArgumentException("sourceAccountId and targetAccountId must be different");
        }
    }
}
