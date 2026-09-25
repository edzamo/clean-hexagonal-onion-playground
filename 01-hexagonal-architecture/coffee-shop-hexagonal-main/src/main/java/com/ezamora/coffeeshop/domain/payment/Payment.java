package com.ezamora.coffeeshop.domain.model.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Pago realizado. Nunca conserva el PAN: solo los últimos 4 dígitos. */
public record Payment(UUID orderId, String last4, String cardHolderName, BigDecimal amount, LocalDate paid) { }
