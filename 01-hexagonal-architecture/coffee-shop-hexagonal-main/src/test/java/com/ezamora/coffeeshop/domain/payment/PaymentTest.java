package com.ezamora.coffeeshop.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ezamora.coffeeshop.domain.exception.InvalidCardException;
import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;

class PaymentTest {

    private static final UUID ORDER = UUID.randomUUID();
    private static final LocalDate DAY = LocalDate.of(2025, 6, 15);

    @Test
    void aValidPaymentIsAccepted() {
        assertThatCode(() -> new Payment(ORDER, "1111", "Ana Perez", BigDecimal.ZERO, DAY)).doesNotThrowAnyException();
    }

    @Test
    void rejectsNullOrderId() {
        assertThatThrownBy(() -> new Payment(null, "1111", "Ana", BigDecimal.TEN, DAY))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Payment(ORDER, "1111", "Ana", null, DAY))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> new Payment(ORDER, "1111", "Ana", new BigDecimal("-0.01"), DAY))
                .isInstanceOf(InvalidOrderException.class).hasMessageContaining("amount");
    }

    @Test
    void rejectsNullPaymentDate() {
        assertThatThrownBy(() -> new Payment(ORDER, "1111", "Ana", BigDecimal.TEN, null))
                .isInstanceOf(InvalidOrderException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "111", "11111", "abcd", "11 1" })
    void rejectsLast4ThatIsNotExactlyFourDigits(String last4) {
        assertThatThrownBy(() -> new Payment(ORDER, last4, "Ana", BigDecimal.TEN, DAY))
                .isInstanceOf(InvalidCardException.class).hasMessageContaining("last4");
    }

    @Test
    void rejectsNullLast4() {
        assertThatThrownBy(() -> new Payment(ORDER, null, "Ana", BigDecimal.TEN, DAY))
                .isInstanceOf(InvalidCardException.class);
    }

    @Test
    void rejectsBlankHolder() {
        assertThatThrownBy(() -> new Payment(ORDER, "1111", " ", BigDecimal.TEN, DAY))
                .isInstanceOf(InvalidCardException.class).hasMessageContaining("holder");
    }

    @Test
    void toStringDoesNotExposeTheHolderName() {
        assertThat(new Payment(ORDER, "1111", "Ana Perez", BigDecimal.TEN, DAY).toString())
                .doesNotContain("Ana Perez").contains("1111");
    }
}
