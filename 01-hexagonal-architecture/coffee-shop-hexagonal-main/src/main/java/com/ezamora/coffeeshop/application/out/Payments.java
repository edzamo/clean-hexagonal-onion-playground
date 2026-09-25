package com.ezamora.coffeeshop.application.out;

import java.util.UUID;

import com.ezamora.coffeeshop.domain.payment.Payment;

public interface Payments {

    Payment findPaymentByOrderId(UUID orderId);

    Payment save(Payment payment);

}
