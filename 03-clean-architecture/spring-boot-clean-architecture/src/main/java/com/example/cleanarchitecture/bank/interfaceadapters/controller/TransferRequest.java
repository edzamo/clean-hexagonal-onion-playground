package com.example.cleanarchitecture.bank.interfaceadapters.controller;

import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.Money;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferCommand;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(@NotNull UUID targetAccountId, @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {

    public TransferCommand toCommand(UUID sourceAccountId) {
        return new TransferCommand(new AccountId(sourceAccountId), new AccountId(targetAccountId), new Money(amount));
    }
}
