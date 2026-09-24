package com.ezamora.coffeeshop.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ezamora.coffeeshop.application.out.PaymentNotFound;
import com.ezamora.coffeeshop.application.out.Payments;
import com.ezamora.coffeeshop.domain.model.payment.Payment;

/** Doble en memoria del puerto de salida {@link Payments}. */
class InMemoryPayments implements Payments {

    final Map<UUID, Payment> store = new HashMap<>();
    final List<String> events;

    InMemoryPayments(List<String> events) {
        this.events = events;
    }

    InMemoryPayments() {
        this(new ArrayList<>());
    }

    @Override
    public Payment findPaymentByOrderId(UUID orderId) {
        var payment = store.get(orderId);
        if (payment == null) {
            throw new PaymentNotFound("Payment not found for order: " + orderId);
        }
        return payment;
    }

    @Override
    public Payment save(Payment payment) {
        events.add("payments.save");
        store.put(payment.orderId(), payment);
        return payment;
    }

    void seed(Payment payment) {
        store.put(payment.orderId(), payment);
    }
}
