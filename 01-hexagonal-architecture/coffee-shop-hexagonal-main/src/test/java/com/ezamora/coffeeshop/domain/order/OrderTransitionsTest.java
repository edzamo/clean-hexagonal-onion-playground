package com.ezamora.coffeeshop.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.exception.OrderStateException;

/** Matriz completa: 6 operaciones x 5 estados. Cada operación solo es válida desde un estado origen. */
class OrderTransitionsTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LineItem LATTE = new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1);

    private static Order orderIn(Status status) {
        return Order.rehydrate(ID, Location.TAKE_AWAY, List.of(LATTE), status);
    }

    private static void assertRejectedFrom(Status status, Status required, Function<Order, ?> operation) {
        assertThatThrownBy(() -> operation.apply(orderIn(status)))
                .isInstanceOf(OrderStateException.class)
                .hasMessageContaining("status is " + status)
                .hasMessageContaining(required.name());
    }

    private static void assertMovesAndKeepsOriginal(Status from, Status to, Function<Order, Order> operation) {
        var original = orderIn(from);

        var result = operation.apply(original);

        assertThat(result).isNotSameAs(original);
        assertThat(result.getStatus()).isEqualTo(to);
        assertThat(result.getId()).isEqualTo(ID);
        assertThat(original.getStatus()).isEqualTo(from);
    }

    // pay

    @Test
    void payMovesPaymentExpectedToPaidAndKeepsTheOriginal() {
        assertMovesAndKeepsOriginal(Status.PAYMENT_EXPECTED, Status.PAID, Order::pay);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "PAYMENT_EXPECTED", mode = Mode.EXCLUDE)
    void payIsRejectedFromEveryOtherState(Status status) {
        assertRejectedFrom(status, Status.PAYMENT_EXPECTED, Order::pay);
    }

    // startPreparing

    @Test
    void startPreparingMovesPaidToPreparingAndKeepsTheOriginal() {
        assertMovesAndKeepsOriginal(Status.PAID, Status.PREPARING, Order::startPreparing);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "PAID", mode = Mode.EXCLUDE)
    void startPreparingIsRejectedFromEveryOtherState(Status status) {
        assertRejectedFrom(status, Status.PAID, Order::startPreparing);
    }

    // finishPreparing

    @Test
    void finishPreparingMovesPreparingToReadyAndKeepsTheOriginal() {
        assertMovesAndKeepsOriginal(Status.PREPARING, Status.READY, Order::finishPreparing);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "PREPARING", mode = Mode.EXCLUDE)
    void finishPreparingIsRejectedFromEveryOtherState(Status status) {
        assertRejectedFrom(status, Status.PREPARING, Order::finishPreparing);
    }

    // take

    @Test
    void takeMovesReadyToTakenAndKeepsTheOriginal() {
        assertMovesAndKeepsOriginal(Status.READY, Status.TAKEN, Order::take);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "READY", mode = Mode.EXCLUDE)
    void takeIsRejectedFromEveryOtherState(Status status) {
        assertRejectedFrom(status, Status.READY, Order::take);
    }

    // assertCancellable

    @Test
    void anOrderWaitingForPaymentIsCancellable() {
        assertThatCode(() -> orderIn(Status.PAYMENT_EXPECTED).assertCancellable()).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "PAYMENT_EXPECTED", mode = Mode.EXCLUDE)
    void cancellingIsRejectedFromEveryOtherState(Status status) {
        assertRejectedFrom(status, Status.PAYMENT_EXPECTED, order -> {
            order.assertCancellable();
            return order;
        });
    }

    // update

    @Test
    void updateReplacesLocationAndItemsKeepingIdAndStatus() {
        var original = orderIn(Status.PAYMENT_EXPECTED);
        var newItems = List.of(new LineItem(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2));

        var updated = original.update(Location.IN_STORE, newItems);

        assertThat(updated.getId()).isEqualTo(ID);
        assertThat(updated.getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
        assertThat(updated.getLocation()).isEqualTo(Location.IN_STORE);
        assertThat(updated.getItems()).isEqualTo(newItems);
    }

    @Test
    void updateDoesNotModifyTheOriginal() {
        var original = orderIn(Status.PAYMENT_EXPECTED);

        original.update(Location.IN_STORE, List.of(new LineItem(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2)));

        assertThat(original.getLocation()).isEqualTo(Location.TAKE_AWAY);
        assertThat(original.getItems()).containsExactly(LATTE);
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = "PAYMENT_EXPECTED", mode = Mode.EXCLUDE)
    void updateIsRejectedFromEveryOtherState(Status status) {
        assertRejectedFrom(status, Status.PAYMENT_EXPECTED, order -> order.update(Location.IN_STORE, List.of(LATTE)));
    }
}
