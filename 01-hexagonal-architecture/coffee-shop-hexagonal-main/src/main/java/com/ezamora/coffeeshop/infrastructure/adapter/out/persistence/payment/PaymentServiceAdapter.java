package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ezamora.coffeeshop.application.out.PaymentNotFound;
import com.ezamora.coffeeshop.application.out.Payments;
import com.ezamora.coffeeshop.domain.payment.Payment;

import lombok.RequiredArgsConstructor;

/** Adaptador de salida del puerto {@link Payments} sobre JPA. */
@Component
@RequiredArgsConstructor
public class PaymentServiceAdapter implements Payments {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment findPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderUuid(orderId)
                .map(PaymentMapper::toDomain)
                .orElseThrow(() -> new PaymentNotFound("Payment not found for order: " + orderId));
    }

    @Override
    public Payment save(Payment payment) {
        return PaymentMapper.toDomain(paymentRepository.save(PaymentMapper.toEntity(payment)));
    }
}
