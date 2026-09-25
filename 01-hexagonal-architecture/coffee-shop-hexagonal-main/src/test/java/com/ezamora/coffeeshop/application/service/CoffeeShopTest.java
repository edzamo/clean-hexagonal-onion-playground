package com.ezamora.coffeeshop.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.application.out.PaymentNotFound;
import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.exception.InvalidCardException;
import com.ezamora.coffeeshop.domain.exception.OrderStateException;
import com.ezamora.coffeeshop.domain.order.LineItem;
import com.ezamora.coffeeshop.domain.order.Order;
import com.ezamora.coffeeshop.domain.payment.CreditCard;
import com.ezamora.coffeeshop.domain.payment.Payment;

class CoffeeShopTest {

    private static final String PAN = "4111111111111111";
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2025-06-15T10:00:00Z"), ZoneOffset.UTC);
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LineItem LATTE = new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1);

    private List<String> events;
    private InMemoryOrders orders;
    private InMemoryPayments payments;
    private CoffeeShop coffeeShop;

    @BeforeEach
    void setUp() {
        events = new ArrayList<>();
        orders = new InMemoryOrders(events);
        payments = new InMemoryPayments(events);
        coffeeShop = new CoffeeShop(orders, payments, CLOCK);
    }

    private Order seeded(Status status) {
        var order = Order.rehydrate(ID, Location.TAKE_AWAY, List.of(LATTE), status);
        orders.seed(order);
        return order;
    }

    private static CreditCard validCard() {
        return new CreditCard("Ana Perez", PAN, Month.DECEMBER, Year.of(2030));
    }

    @Nested
    class PlaceAndFindOrder {

        @Test
        void placeOrderPersistsTheOrder() {
            var order = Order.create(ID, Location.TAKE_AWAY, List.of(LATTE));

            var placed = coffeeShop.placeOrder(order);

            assertThat(placed.getId()).isEqualTo(ID);
            assertThat(orders.store).containsKey(ID);
        }

        @Test
        void findOrderByIdReturnsTheStoredOrder() {
            var order = seeded(Status.PAYMENT_EXPECTED);

            assertThat(coffeeShop.findOrderById(ID)).isSameAs(order);
        }

        @Test
        void findOrderByIdFailsWhenMissing() {
            assertThatThrownBy(() -> coffeeShop.findOrderById(ID)).isInstanceOf(OrderNotFound.class);
        }

    }

    @Nested
    class UpdateOrder {

        @Test
        void updateOrderReplacesItemsWhilePaymentIsExpected() {
            seeded(Status.PAYMENT_EXPECTED);
            var replacement = Order.create(Location.IN_STORE,
                    List.of(new LineItem(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2)));

            var updated = coffeeShop.updateOrder(ID, replacement);

            assertThat(updated.getId()).isEqualTo(ID);
            assertThat(updated.getLocation()).isEqualTo(Location.IN_STORE);
            assertThat(orders.store.get(ID).getItems()).isEqualTo(replacement.getItems());
        }

        @Test
        void updateOrderFailsWhenAlreadyPaid() {
            seeded(Status.PAID);
            var replacement = Order.create(Location.IN_STORE, List.of(LATTE));

            assertThatThrownBy(() -> coffeeShop.updateOrder(ID, replacement)).isInstanceOf(OrderStateException.class);
            assertThat(events).isEmpty();
        }

        @Test
        void updateOrderFailsWhenMissing() {
            var replacement = Order.create(Location.IN_STORE, List.of(LATTE));

            assertThatThrownBy(() -> coffeeShop.updateOrder(ID, replacement)).isInstanceOf(OrderNotFound.class);
        }

    }

    @Nested
    class CancelOrder {

        @Test
        void cancelOrderDeletesAnUnpaidOrder() {
            seeded(Status.PAYMENT_EXPECTED);

            coffeeShop.cancelOrder(ID);

            assertThat(orders.store).doesNotContainKey(ID);
            assertThat(events).containsExactly("orders.delete");
        }

        @Test
        void cancelOrderFailsWhenPaidAndDeletesNothing() {
            seeded(Status.PAID);

            assertThatThrownBy(() -> coffeeShop.cancelOrder(ID)).isInstanceOf(OrderStateException.class);
            assertThat(orders.store).containsKey(ID);
            assertThat(events).isEmpty();
        }

        @Test
        void cancelOrderFailsWhenMissing() {
            assertThatThrownBy(() -> coffeeShop.cancelOrder(ID)).isInstanceOf(OrderNotFound.class);
        }

    }

    @Nested
    class PayOrder {

        @Test
        void payOrderSavesThePaymentFirstAndThenThePaidOrder() {
            seeded(Status.PAYMENT_EXPECTED);

            var payment = coffeeShop.payOrder(ID, validCard());

            assertThat(events).containsExactly("payments.save", "orders.save:PAID");
            assertThat(orders.store.get(ID).getStatus()).isEqualTo(Status.PAID);
            assertThat(payment.orderId()).isEqualTo(ID);
            assertThat(payment.amount()).isEqualByComparingTo(new BigDecimal("5.0"));
            assertThat(payment.paid()).isEqualTo(LocalDate.of(2025, 6, 15));
            assertThat(payment.last4()).isEqualTo("1111");
            assertThat(payment.cardHolderName()).isEqualTo("Ana Perez");
        }

        @Test
        void payOrderWithExpiredCardFailsAndSavesNothing() {
            seeded(Status.PAYMENT_EXPECTED);
            var expired = new CreditCard("Ana Perez", PAN, Month.MAY, Year.of(2025));

            assertThatThrownBy(() -> coffeeShop.payOrder(ID, expired)).isInstanceOf(InvalidCardException.class);
            assertThat(events).isEmpty();
            assertThat(orders.store.get(ID).getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
        }

        @Test
        void payOrderFailsWhenAlreadyPaidAndSavesNothing() {
            seeded(Status.PAID);

            assertThatThrownBy(() -> coffeeShop.payOrder(ID, validCard())).isInstanceOf(OrderStateException.class);
            assertThat(events).isEmpty();
        }

        @Test
        void payOrderFailsWhenMissing() {
            assertThatThrownBy(() -> coffeeShop.payOrder(ID, validCard())).isInstanceOf(OrderNotFound.class);
            assertThat(events).isEmpty();
        }

    }

    @Nested
    class ReadReceipt {

        @Test
        void readReceiptReturnsOrderCostAndPaymentDate() {
            seeded(Status.PAID);
            payments.seed(new Payment(ID, "1111", "Ana Perez", new BigDecimal("5.0"), LocalDate.of(2025, 6, 1)));

            var receipt = coffeeShop.readReceipt(ID);

            assertThat(receipt.amount()).isEqualByComparingTo(new BigDecimal("5.0"));
            assertThat(receipt.paid()).isEqualTo(LocalDate.of(2025, 6, 1));
        }

        @Test
        void readReceiptFailsWithPaymentNotFoundWhenTheOrderWasNeverPaid() {
            seeded(Status.PAYMENT_EXPECTED);

            assertThatThrownBy(() -> coffeeShop.readReceipt(ID)).isInstanceOf(PaymentNotFound.class);
        }

        @Test
        void readReceiptFailsWhenOrderIsMissing() {
            assertThatThrownBy(() -> coffeeShop.readReceipt(ID)).isInstanceOf(OrderNotFound.class);
        }

    }

    @Nested
    class TakeOrder {

        @Test
        void takeOrderMovesReadyOrderToTaken() {
            seeded(Status.READY);

            var taken = coffeeShop.takeOrder(ID);

            assertThat(taken.getStatus()).isEqualTo(Status.TAKEN);
            assertThat(orders.store.get(ID).getStatus()).isEqualTo(Status.TAKEN);
        }

        @Test
        void takeOrderFailsWhenTheOrderIsMissing() {
            assertThatThrownBy(() -> coffeeShop.takeOrder(ID)).isInstanceOf(OrderNotFound.class);
        }

        @Test
        void takeOrderFailsWhenNotReady() {
            seeded(Status.PREPARING);

            assertThatThrownBy(() -> coffeeShop.takeOrder(ID)).isInstanceOf(OrderStateException.class);
            assertThat(events).isEmpty();
        }
    }
}
