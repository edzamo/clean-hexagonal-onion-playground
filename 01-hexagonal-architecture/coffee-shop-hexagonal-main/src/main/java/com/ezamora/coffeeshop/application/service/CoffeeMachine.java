package com.ezamora.coffeeshop.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ezamora.coffeeshop.application.in.PreparingCoffee;
import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.domain.order.Order;

/** Casos de uso de preparación (un solo puerto de salida: sin transacción explícita). */
@Service
public class CoffeeMachine implements PreparingCoffee {

    private final Orders orders;

    public CoffeeMachine(Orders orders) {
        this.orders = orders;
    }

    @Override
    public Order startPreparingOrder(UUID orderId) {
        return orders.save(orders.findOrderById(orderId).startPreparing());
    }

    @Override
    public Order finishPreparingOrder(UUID orderId) {
        return orders.save(orders.findOrderById(orderId).finishPreparing());
    }
}
