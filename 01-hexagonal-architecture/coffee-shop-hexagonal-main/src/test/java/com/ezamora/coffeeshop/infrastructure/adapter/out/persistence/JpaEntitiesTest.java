package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.DrinkJpa;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.MilkJpa;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderItemJpaEntity;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.SizeJpa;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment.PaymentJpaEntity;

/** Identidad e impresión seguras de las entidades JPA (sin @Data). */
class JpaEntitiesTest {

    private static final UUID ID = UUID.randomUUID();

    private static OrderItemJpaEntity item() {
        return OrderItemJpaEntity.builder().drink(DrinkJpa.LATTE).milk(MilkJpa.WHOLE).size(SizeJpa.LARGE).quantity(1).build();
    }

    @Test
    void ordersWithTheSameUuidAreEqualEvenWithDifferentInternalIds() {
        var a = OrderJpaEntity.builder().id(1L).uuid(ID).build();
        var b = OrderJpaEntity.builder().id(2L).uuid(ID).totalAmount(BigDecimal.TEN).build();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void ordersWithDifferentUuidsAreDifferent() {
        assertThat(OrderJpaEntity.builder().uuid(ID).build())
                .isNotEqualTo(OrderJpaEntity.builder().uuid(UUID.randomUUID()).build());
    }

    @Test
    void paymentsAreIdentifiedByTheirOrderUuid() {
        var a = PaymentJpaEntity.builder().id(1L).orderUuid(ID).last4("1111").build();
        var b = PaymentJpaEntity.builder().id(2L).orderUuid(ID).last4("2222").build();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void paymentToStringDoesNotExposeThePersonalData() {
        var payment = PaymentJpaEntity.builder().orderUuid(ID).last4("1111").cardHolderName("Ana Perez")
                .amount(BigDecimal.TEN).paymentDate(LocalDate.now()).build();

        assertThat(payment.toString()).doesNotContain("Ana Perez");
    }

    @Test
    void bidirectionalOrderAndItemsCanBePrintedAndHashedWithoutRecursion() {
        var order = OrderJpaEntity.builder().uuid(ID).build();
        order.setItems(List.of(item()));

        assertThat(order.toString()).isNotBlank();
        assertThat(order.getItems().get(0).toString()).doesNotContain(ID.toString());
        assertThat(order.hashCode()).isEqualTo(OrderJpaEntity.builder().uuid(ID).build().hashCode());
    }
}
