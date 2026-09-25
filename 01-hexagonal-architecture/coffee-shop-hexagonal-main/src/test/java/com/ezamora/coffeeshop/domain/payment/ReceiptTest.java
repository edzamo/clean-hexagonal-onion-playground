package com.ezamora.coffeeshop.domain.payment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;

class ReceiptTest {

    @Test
    void aValidReceiptIsAccepted() {
        assertThatCode(() -> new Receipt(BigDecimal.ZERO, LocalDate.of(2025, 6, 15))).doesNotThrowAnyException();
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Receipt(null, LocalDate.now())).isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> new Receipt(new BigDecimal("-1"), LocalDate.now()))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void rejectsNullDate() {
        assertThatThrownBy(() -> new Receipt(BigDecimal.TEN, null)).isInstanceOf(InvalidOrderException.class);
    }
}
