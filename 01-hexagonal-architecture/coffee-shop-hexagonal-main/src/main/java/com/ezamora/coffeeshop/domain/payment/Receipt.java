package com.ezamora.coffeeshop.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;

public record Receipt(BigDecimal amount, LocalDate paid) {

    public Receipt {
        if (amount == null || amount.signum() < 0) {
            throw new InvalidOrderException("Receipt amount must not be null or negative");
        }
        if (paid == null) {
            throw new InvalidOrderException("Receipt date must not be null");
        }
    }
}
