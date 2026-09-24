package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.ezamora.coffeeshop.application.out.Payments;
import com.ezamora.coffeeshop.contract.PaymentsContract;
import com.ezamora.coffeeshop.domain.model.payment.Payment;

/** Contrato del puerto {@code Payments} (heredado) más las garantías propias de JPA, con H2. */
@DataJpaTest
@ActiveProfiles("test")
@Import(PaymentServiceAdapter.class)
class PaymentServiceAdapterTest extends PaymentsContract {

    @Autowired
    private PaymentServiceAdapter adapter;
    @Autowired
    private PaymentRepository repository;
    @Autowired
    private TestEntityManager em;

    @Override
    protected Payments payments() {
        return adapter;
    }

    @Override
    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    private PaymentJpaEntity storedEntity() {
        var payment = new Payment(UUID.randomUUID(), "1111", "Ana Perez", new BigDecimal("9.50"), LocalDate.of(2025, 6, 15));
        adapter.save(payment);
        flushAndClear();
        return repository.findAll().get(0);
    }

    @Test
    void storesCreditCardAsMethod() {
        assertThat(storedEntity().getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    void storesCompletedAsStatus() {
        assertThat(storedEntity().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void storesOnlyTheLastFourDigits() {
        assertThat(storedEntity().getLast4()).isEqualTo("1111");
    }
}
