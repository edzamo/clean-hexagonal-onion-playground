package com.example.cleanarchitecture.bank.interfaceadapters.controller;

import com.example.cleanarchitecture.bank.entities.Money;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount) {

    public Money toMoney() {
        return new Money(amount);
    }
}
