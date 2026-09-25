package com.ezamora.coffeeshop.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.exception.OrderStateException;
import com.ezamora.coffeeshop.domain.order.LineItem;
import com.ezamora.coffeeshop.domain.order.Order;

class CoffeeMachineTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private InMemoryOrders orders;
    private CoffeeMachine coffeeMachine;

    @BeforeEach
    void setUp() {
        orders = new InMemoryOrders();
        coffeeMachine = new CoffeeMachine(orders);
    }

    private void seeded(Status status) {
        orders.seed(Order.rehydrate(ID, Location.IN_STORE,
                List.of(new LineItem(Drink.LATTE, Milk.WHOLE, Size.SMALL, 1)), status));
    }

    @Test
    void startPreparingReturnsThePreparingOrder() {
        seeded(Status.PAID);

        assertThat(coffeeMachine.startPreparingOrder(ID).getStatus()).isEqualTo(Status.PREPARING);
    }

    @Test
    void startPreparingSavesThePreparingOrder() {
        seeded(Status.PAID);

        coffeeMachine.startPreparingOrder(ID);

        assertThat(orders.store.get(ID).getStatus()).isEqualTo(Status.PREPARING);
    }

    @Test
    void startPreparingFailsWhenNotPaid() {
        seeded(Status.PAYMENT_EXPECTED);

        assertThatThrownBy(() -> coffeeMachine.startPreparingOrder(ID)).isInstanceOf(OrderStateException.class);
        assertThat(orders.events).isEmpty();
    }

    @Test
    void finishPreparingReturnsTheReadyOrder() {
        seeded(Status.PREPARING);

        assertThat(coffeeMachine.finishPreparingOrder(ID).getStatus()).isEqualTo(Status.READY);
    }

    @Test
    void finishPreparingSavesTheReadyOrder() {
        seeded(Status.PREPARING);

        coffeeMachine.finishPreparingOrder(ID);

        assertThat(orders.store.get(ID).getStatus()).isEqualTo(Status.READY);
    }

    @Test
    void finishPreparingFailsWhenNotPreparing() {
        seeded(Status.PAID);

        assertThatThrownBy(() -> coffeeMachine.finishPreparingOrder(ID)).isInstanceOf(OrderStateException.class);
    }

    @Test
    void startPreparingFailsWhenOrderIsMissing() {
        assertThatThrownBy(() -> coffeeMachine.startPreparingOrder(ID)).isInstanceOf(OrderNotFound.class);
    }

    @Test
    void finishPreparingFailsWhenOrderIsMissing() {
        assertThatThrownBy(() -> coffeeMachine.finishPreparingOrder(ID)).isInstanceOf(OrderNotFound.class);
    }
}
