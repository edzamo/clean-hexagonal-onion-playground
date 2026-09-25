package com.ezamora.coffeeshop.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.domain.exception.InvalidCardException;

class CreditCardTest {

    private static final String VALID_PAN = "4111111111111111";
    private static final Clock JUNE_2025 = Clock.fixed(Instant.parse("2025-06-15T10:00:00Z"), ZoneOffset.UTC);

    private static CreditCard card(String holder, String pan, Month month, int year) {
        return new CreditCard(holder, pan, month, Year.of(year));
    }

    @Test
    void acceptsValidCardAndExposesLastFourDigits() {
        var card = card("Ana Perez", VALID_PAN, Month.DECEMBER, 2030);

        assertThat(card.last4()).isEqualTo("1111");
        assertThat(card.getCardHolderName()).isEqualTo("Ana Perez");
    }

    @Test
    void rejectsNumberFailingLuhn() {
        assertThatThrownBy(() -> card("Ana", "4111111111111112", Month.DECEMBER, 2030))
                .isInstanceOf(InvalidCardException.class).hasMessageContaining("Luhn");
    }

    @Test
    void rejectsNonNumericOrNullNumber() {
        assertThatThrownBy(() -> card("Ana", "4111-abcd", Month.DECEMBER, 2030))
                .isInstanceOf(InvalidCardException.class);
        assertThatThrownBy(() -> card("Ana", null, Month.DECEMBER, 2030))
                .isInstanceOf(InvalidCardException.class);
    }

    @Test
    void rejectsBlankOrNullHolder() {
        assertThatThrownBy(() -> card("  ", VALID_PAN, Month.DECEMBER, 2030))
                .isInstanceOf(InvalidCardException.class).hasMessageContaining("holder");
        assertThatThrownBy(() -> card(null, VALID_PAN, Month.DECEMBER, 2030))
                .isInstanceOf(InvalidCardException.class);
    }

    @Test
    void rejectsMissingExpiry() {
        assertThatThrownBy(() -> new CreditCard("Ana", VALID_PAN, null, Year.of(2030)))
                .isInstanceOf(InvalidCardException.class);
        assertThatThrownBy(() -> new CreditCard("Ana", VALID_PAN, Month.MAY, null))
                .isInstanceOf(InvalidCardException.class);
    }

    @Test
    void cardExpiredInPastMonthIsRejectedAgainstClock() {
        var expired = card("Ana", VALID_PAN, Month.MAY, 2025);

        assertThatThrownBy(() -> expired.assertNotExpired(JUNE_2025))
                .isInstanceOf(InvalidCardException.class).hasMessageContaining("expired");
    }

    @Test
    void cardIsValidThroughTheEndOfItsExpiryMonth() {
        var lastValidMonth = card("Ana", VALID_PAN, Month.JUNE, 2025);

        assertThatCode(() -> lastValidMonth.assertNotExpired(JUNE_2025)).doesNotThrowAnyException();
    }

    @Test
    void toStringNeverContainsTheFullNumber() {
        var text = card("Ana", VALID_PAN, Month.DECEMBER, 2030).toString();

        assertThat(text).doesNotContain(VALID_PAN).contains("1111");
    }
}
