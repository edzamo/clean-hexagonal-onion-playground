package com.example.cleanarchitecture.bank.interfaceadapters.controller;

import com.example.cleanarchitecture.bank.usecases.port.in.TransferResult;

public record TransferResponse(AccountResponse sourceAccount, AccountResponse targetAccount) {

    public static TransferResponse from(TransferResult result) {
        return new TransferResponse(
                AccountResponse.from(result.sourceAccount()), AccountResponse.from(result.targetAccount()));
    }
}
