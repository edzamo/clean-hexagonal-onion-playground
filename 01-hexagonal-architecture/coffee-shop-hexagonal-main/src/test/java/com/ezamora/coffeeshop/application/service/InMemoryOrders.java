package com.ezamora.coffeeshop.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.domain.model.order.Order;

/** Doble en memoria del puerto de salida {@link Orders}; registra los eventos en orden. */
class InMemoryOrders implements Orders {

    final Map<UUID, Order> store = new HashMap<>();
    final List<String> events;

    InMemoryOrders(List<String> events) {
        this.events = events;
    }

    InMemoryOrders() {
        this(new ArrayList<>());
    }

    @Override
    public Order findOrderById(UUID orderId) {
        var order = store.get(orderId);
        if (order == null) {
            throw new OrderNotFound("Order not found with id: " + orderId);
        }
        return order;
    }

    @Override
    public Order save(Order order) {
        events.add("orders.save:" + order.getStatus());
        var stored = Order.rehydrate(order.getId(), order.getLocation(), order.getItems(), order.getStatus());
        store.put(stored.getId(), stored);
        return stored;
    }

    @Override
    public void deleteById(UUID orderId) {
        if (!store.containsKey(orderId)) {
            throw new OrderNotFound("Cannot delete an order that does not exist: " + orderId);
        }
        events.add("orders.delete");
        store.remove(orderId);
    }

    /** Siembra estado inicial sin registrar eventos. */
    void seed(Order order) {
        store.put(order.getId(), order);
    }
}
