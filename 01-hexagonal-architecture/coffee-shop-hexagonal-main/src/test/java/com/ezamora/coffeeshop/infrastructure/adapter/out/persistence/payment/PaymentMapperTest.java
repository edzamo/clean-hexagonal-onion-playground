package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.domain.payment.Payment;

class PaymentMapperTest {

    private static final Payment PAYMENT = new Payment(UUID.randomUUID(), "1111", "Ana Perez",
            new BigDecimal("9.50"), LocalDate.of(2025, 6, 15));

    @Test
    void toEntityCopiesTheDomainFields() {
        var entity = PaymentMapper.toEntity(PAYMENT);

        assertThat(entity.getOrderUuid()).isEqualTo(PAYMENT.orderId());
        assertThat(entity.getLast4()).isEqualTo("1111");
        assertThat(entity.getCardHolderName()).isEqualTo("Ana Perez");
        assertThat(entity.getAmount()).isEqualByComparingTo("9.50");
        assertThat(entity.getPaymentDate()).isEqualTo(LocalDate.of(2025, 6, 15));
    }

    @Test
    void toEntityFixesMethodAndStatus() {
        var entity = PaymentMapper.toEntity(PAYMENT);

        assertThat(entity.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(entity.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void roundTripPreservesThePayment() {
        assertThat(PaymentMapper.toDomain(PaymentMapper.toEntity(PAYMENT))).isEqualTo(PAYMENT);
    }
}
