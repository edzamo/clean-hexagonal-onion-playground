package com.ezamora.coffeeshop.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.ezamora.coffeeshop.domain.exception.InvalidCardException;
import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;

/** Pago realizado. Nunca conserva el PAN: solo los últimos 4 dígitos. */
public record Payment(UUID orderId, String last4, String cardHolderName, BigDecimal amount, LocalDate paid) {

    public Payment {
        if (orderId == null) {
            throw new InvalidOrderException("Payment order id must not be null");
        }
        if (last4 == null || !last4.matches("\\d{4}")) {
            throw new InvalidCardException("Payment last4 must be exactly four digits");
        }
        if (cardHolderName == null || cardHolderName.isBlank()) {
            throw new InvalidCardException("Payment card holder name must not be blank");
        }
        if (amount == null || amount.signum() < 0) {
            throw new InvalidOrderException("Payment amount must not be null or negative");
        }
        if (paid == null) {
            throw new InvalidOrderException("Payment date must not be null");
        }
    }

    /** Sin el titular (dato personal) en logs o trazas. */
    @Override
    public String toString() {
        return "Payment[orderId=" + orderId + ", last4=" + last4 + ", amount=" + amount + ", paid=" + paid + "]";
    }
}
