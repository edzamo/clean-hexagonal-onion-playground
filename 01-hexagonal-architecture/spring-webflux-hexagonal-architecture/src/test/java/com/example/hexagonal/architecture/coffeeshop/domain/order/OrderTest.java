package com.example.hexagonal.architecture.coffeeshop.domain.order;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private Order newOrder() {
        return new Order(Location.TAKE_AWAY, List.of(new LineItem(Drink.LATTE, Milk.SOY, Size.MEDIUM, 2)));
    }

    @Test
    void newOrderStartsAsPaymentExpected() {
        assertThat(newOrder().getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
    }

    @Test
    void requiresAtLeastOneLineItem() {
        assertThatThrownBy(() -> new Order(Location.TAKE_AWAY, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fullHappyPathLifecycle() {
        Order order = newOrder();

        order.pay();
        order.prepare();
        order.markReady();
        order.take();

        assertThat(order.getStatus()).isEqualTo(Status.TAKEN);
    }

    @Test
    void payFailsWhenNotPaymentExpected() {
        Order order = newOrder();
        order.pay();

        assertThatThrownBy(order::pay)
                .isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    void prepareFailsWhenNotPaid() {
        Order order = newOrder();

        assertThatThrownBy(order::prepare)
                .isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    void takeFailsWhenNotReady() {
        Order order = newOrder();
        order.pay();

        assertThatThrownBy(order::take)
                .isInstanceOf(InvalidOrderTransitionException.class);
    }
}
