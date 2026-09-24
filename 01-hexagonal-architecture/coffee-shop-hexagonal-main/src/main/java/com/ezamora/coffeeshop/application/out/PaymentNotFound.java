package com.ezamora.coffeeshop.application.out;

/** No existe un pago para la orden solicitada. */
public class PaymentNotFound extends RuntimeException {

    public PaymentNotFound(String message) {
        super(message);
    }
}
