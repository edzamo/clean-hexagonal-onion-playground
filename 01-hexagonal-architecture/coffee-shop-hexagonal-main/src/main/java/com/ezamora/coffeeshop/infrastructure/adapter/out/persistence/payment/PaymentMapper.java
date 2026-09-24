package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment;

import com.ezamora.coffeeshop.domain.model.payment.Payment;

/** Mapper entre el {@link Payment} de dominio y {@link PaymentJpaEntity}. */
final class PaymentMapper {

    private PaymentMapper() {
    }

    static PaymentJpaEntity toEntity(Payment payment) {
        return PaymentJpaEntity.builder()
                .orderUuid(payment.orderId())
                .last4(payment.last4())
                .cardHolderName(payment.cardHolderName())
                .amount(payment.amount())
                .paymentDate(payment.paid())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .build();
    }

    static Payment toDomain(PaymentJpaEntity entity) {
        return new Payment(entity.getOrderUuid(), entity.getLast4(), entity.getCardHolderName(),
                entity.getAmount(), entity.getPaymentDate());
    }
}
