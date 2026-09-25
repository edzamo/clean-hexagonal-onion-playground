package com.ezamora.coffeeshop.domain.payment;

import java.time.Clock;
import java.time.Month;
import java.time.YearMonth;
import java.time.Year;

import com.ezamora.coffeeshop.domain.exception.InvalidCardException;

/**
 * Tarjeta de crédito. Valida formato (Luhn) y titular al construirse; la caducidad se
 * comprueba contra un {@link Clock} recibido. El número completo (PAN) no se expone:
 * solo {@link #last4()} y {@link #toString()} enmascarado.
 */
public final class CreditCard {

    private final String cardHolderName;
    private final String cardNumber;
    private final YearMonth expiry;

    public CreditCard(String cardHolderName, String cardNumber, Month expiryMonth, Year expiryYear) {
        if (cardHolderName == null || cardHolderName.isBlank()) {
            throw new InvalidCardException("Card holder name must not be blank");
        }
        if (cardNumber == null || !cardNumber.matches("\\d{12,19}") || !passesLuhn(cardNumber)) {
            throw new InvalidCardException("Card number is invalid (digits only and Luhn check required)");
        }
        if (expiryMonth == null || expiryYear == null) {
            throw new InvalidCardException("Card expiry month and year are required");
        }
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.expiry = YearMonth.of(expiryYear.getValue(), expiryMonth);
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String last4() {
        return cardNumber.substring(cardNumber.length() - 4);
    }

    /** La tarjeta es válida hasta el final de su mes de caducidad. */
    public void assertNotExpired(Clock clock) {
        if (expiry.isBefore(YearMonth.now(clock))) {
            throw new InvalidCardException("Card is expired since " + expiry);
        }
    }

    private static boolean passesLuhn(String number) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    @Override
    public String toString() {
        return "CreditCard[holder=" + cardHolderName + ", number=**** **** **** " + last4() + ", expiry=" + expiry + "]";
    }
}
