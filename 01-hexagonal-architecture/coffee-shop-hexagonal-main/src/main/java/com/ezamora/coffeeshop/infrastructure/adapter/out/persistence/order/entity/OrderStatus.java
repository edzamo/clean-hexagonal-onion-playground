package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity;

/**
 * Estado persistido: mismos 5 estados que el dominio, uno a uno.
 * No existe CANCELLED: cancelar una orden la elimina (Orders.deleteById).
 */
public enum OrderStatus {

    PAYMENT_EXPECTED,
    PAID,
    PREPARING,
    READY,
    TAKEN
}
