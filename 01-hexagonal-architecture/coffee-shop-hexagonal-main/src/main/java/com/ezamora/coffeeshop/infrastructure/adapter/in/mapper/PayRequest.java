package com.ezamora.coffeeshop.infrastructure.adapter.in.mapper;

import java.time.Month;
import java.time.Year;

import com.ezamora.coffeeshop.domain.model.payment.CreditCard;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de tarjeta para pagar. El PAN nunca se imprime: {@link #toString()} lo enmascara. */
public record PayRequest(
        @NotBlank @Size(max = 255) String cardHolderName,
        @NotBlank @Pattern(regexp = "\\d{12,19}") String cardNumber,
        @NotNull @Min(1) @Max(12) Integer expiryMonth,
        @NotNull @Min(2000) @Max(2100) Integer expiryYear) {

    public CreditCard toDomain() {
        return new CreditCard(cardHolderName, cardNumber, Month.of(expiryMonth), Year.of(expiryYear));
    }

    @Override
    public String toString() {
        var last4 = cardNumber == null || cardNumber.length() < 4 ? "" : cardNumber.substring(cardNumber.length() - 4);
        return "PayRequest[cardHolderName=" + cardHolderName + ", cardNumber=************" + last4
                + ", expiryMonth=" + expiryMonth + ", expiryYear=" + expiryYear + "]";
    }
}
