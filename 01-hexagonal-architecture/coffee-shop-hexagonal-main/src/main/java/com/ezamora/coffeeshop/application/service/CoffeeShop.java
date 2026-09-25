package com.ezamora.coffeeshop.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezamora.coffeeshop.application.in.OrderingCoffee;
import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.application.out.Payments;
import com.ezamora.coffeeshop.domain.order.Order;
import com.ezamora.coffeeshop.domain.payment.CreditCard;
import com.ezamora.coffeeshop.domain.payment.Payment;
import com.ezamora.coffeeshop.domain.payment.Receipt;

/**
 * Casos de uso de pedido de café. Anotaciones de Spring permitidas: {@code @Service} y, solo en {@code payOrder}, {@code @Transactional}.
 */
@Service
public class CoffeeShop implements OrderingCoffee {

    private final Orders orders;
    private final Payments payments;
    private final Clock clock;

    public CoffeeShop(Orders orders, Payments payments, Clock clock) {
        this.orders = orders;
        this.payments = payments;
        this.clock = clock;
    }

    @Override
    public Order placeOrder(Order order) {
        return orders.save(order);
    }

    @Override
    public Order updateOrder(UUID orderId, Order order) {
        var existing = orders.findOrderById(orderId);
        return orders.save(existing.update(order.getLocation(), order.getItems()));
    }

    @Override
    public void cancelOrder(UUID orderId) {
        orders.findOrderById(orderId).assertCancellable();
        orders.deleteById(orderId);
    }

    /** Escribe en dos puertos (pago y orden): una sola transacción, sin estado parcial (INV-18). */
    @Override
    @Transactional
    public Payment payOrder(UUID orderId, CreditCard creditCard) {
        var order = orders.findOrderById(orderId);
        creditCard.assertNotExpired(clock);
        var paidOrder = order.pay();

        // Se guarda primero el pago y después la orden pagada.
        var payment = payments.save(new Payment(orderId, creditCard.last4(),
                creditCard.getCardHolderName(), order.getCost(), LocalDate.now(clock)));
        orders.save(paidOrder);
        return payment;
    }

    @Override
    public Receipt readReceipt(UUID orderId) {
        var order = orders.findOrderById(orderId);
        var payment = payments.findPaymentByOrderId(orderId);
        return new Receipt(order.getCost(), payment.paid());
    }

    @Override
    public Order takeOrder(UUID orderId) {
        return orders.save(orders.findOrderById(orderId).take());
    }

    @Override
    public Order findOrderById(UUID orderId) {
        return orders.findOrderById(orderId);
    }
}
