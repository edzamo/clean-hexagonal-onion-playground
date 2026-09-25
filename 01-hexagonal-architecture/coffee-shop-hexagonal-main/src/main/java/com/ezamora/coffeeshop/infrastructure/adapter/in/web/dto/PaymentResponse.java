package com.ezamora.coffeeshop.infrastructure.adapter.in.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.ezamora.coffeeshop.domain.model.payment.Payment;

/** Respuesta de pago: solo los últimos 4 dígitos de la tarjeta. */
public record PaymentResponse(UUID orderId, String last4, String cardHolderName, BigDecimal amount, LocalDate paid) {

    public static PaymentResponse fromDomain(Payment payment) {
        return new PaymentResponse(payment.orderId(), payment.last4(), payment.cardHolderName(),
                payment.amount(), payment.paid());
    }
}
