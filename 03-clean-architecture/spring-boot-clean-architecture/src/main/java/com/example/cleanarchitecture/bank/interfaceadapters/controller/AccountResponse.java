package com.example.cleanarchitecture.bank.interfaceadapters.controller;

import com.example.cleanarchitecture.bank.entities.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID id, String holderName, BigDecimal balance, String status) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId().value(), account.getHolderName(), account.getBalance().amount(), account.getStatus().name());
    }
}
