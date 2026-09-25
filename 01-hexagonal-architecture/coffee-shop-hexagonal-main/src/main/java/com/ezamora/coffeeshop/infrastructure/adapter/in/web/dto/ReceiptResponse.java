package com.ezamora.coffeeshop.infrastructure.adapter.in.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ezamora.coffeeshop.domain.model.payment.Receipt;

public record ReceiptResponse(BigDecimal amount, LocalDate paid) {

    public static ReceiptResponse fromDomain(Receipt receipt) {
        return new ReceiptResponse(receipt.amount(), receipt.paid());
    }
}
