package com.ezamora.coffeeshop.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.ezamora.coffeeshop.application.out.PaymentNotFound;
import com.ezamora.coffeeshop.application.out.Payments;
import com.ezamora.coffeeshop.domain.payment.Payment;

/** Contrato del puerto {@link Payments}: lo ejecutan el fake en memoria y el adaptador JPA. */
@Transactional
public abstract class PaymentsContract {

    protected abstract Payments payments();

    protected void flushAndClear() {
    }

    private static Payment payment() {
        return new Payment(UUID.randomUUID(), "1111", "Ana Perez", new BigDecimal("9.50"), LocalDate.of(2025, 6, 15));
    }

    @Test
    void saveReturnsTheStoredPayment() {
        var payment = payment();

        assertThat(payments().save(payment)).isEqualTo(payment);
    }

    @Test
    void aSavedPaymentCanBeFoundByOrderId() {
        var payment = payment();
        payments().save(payment);
        flushAndClear();

        assertThat(payments().findPaymentByOrderId(payment.orderId())).isEqualTo(payment);
    }

    @Test
    void findingAMissingPaymentFailsWithPaymentNotFound() {
        assertThatThrownBy(() -> payments().findPaymentByOrderId(UUID.randomUUID()))
                .isInstanceOf(PaymentNotFound.class);
    }
}
