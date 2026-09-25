package com.ezamora.coffeeshop.domain.model.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.domain.model.enums.Drink;
import com.ezamora.coffeeshop.domain.model.enums.Location;
import com.ezamora.coffeeshop.domain.model.enums.Milk;
import com.ezamora.coffeeshop.domain.model.enums.Size;
import com.ezamora.coffeeshop.domain.model.enums.Status;
import com.ezamora.coffeeshop.domain.model.exception.OrderStateException;

class OrderTransitionsTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LineItem LATTE = new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1);

    private static Order orderIn(Status status) {
        return Order.rehydrate(ID, Location.TAKE_AWAY, List.of(LATTE), status);
    }

    @Test
    void payMovesToPaidAndReturnsNewOrder() {
        var original = orderIn(Status.PAYMENT_EXPECTED);

        var paid = original.pay();

        assertThat(paid).isNotSameAs(original);
        assertThat(paid.getStatus()).isEqualTo(Status.PAID);
        assertThat(paid.getId()).isEqualTo(ID);
        assertThat(original.getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
    }

    @Test
    void startPreparingMovesPaidToPreparing() {
        assertThat(orderIn(Status.PAID).startPreparing().getStatus()).isEqualTo(Status.PREPARING);
    }

    @Test
    void finishPreparingMovesPreparingToReady() {
        assertThat(orderIn(Status.PREPARING).finishPreparing().getStatus()).isEqualTo(Status.READY);
    }

    @Test
    void takeMovesReadyToTaken() {
        assertThat(orderIn(Status.READY).take().getStatus()).isEqualTo(Status.TAKEN);
    }

    @Test
    void payFailsWhenAlreadyPaidWithRealStatusInMessage() {
        assertThatThrownBy(() -> orderIn(Status.PREPARING).pay())
                .isInstanceOf(OrderStateException.class)
                .hasMessageContaining("PREPARING").hasMessageContaining("PAYMENT_EXPECTED");
    }

    @Test
    void startPreparingFailsUnlessPaid() {
        assertThatThrownBy(() -> orderIn(Status.PAYMENT_EXPECTED).startPreparing())
                .isInstanceOf(OrderStateException.class).hasMessageContaining("PAYMENT_EXPECTED");
        assertThatThrownBy(() -> orderIn(Status.READY).startPreparing())
                .isInstanceOf(OrderStateException.class);
    }

    @Test
    void finishPreparingFailsUnlessPreparing() {
        assertThatThrownBy(() -> orderIn(Status.PAID).finishPreparing())
                .isInstanceOf(OrderStateException.class).hasMessageContaining("PAID");
    }

    @Test
    void takeFailsUnlessReady() {
        assertThatThrownBy(() -> orderIn(Status.PREPARING).take())
                .isInstanceOf(OrderStateException.class).hasMessageContaining("PREPARING");
    }

    @Test
    void anUnpaidOrderIsCancellable() {
        assertThatCode(() -> orderIn(Status.PAYMENT_EXPECTED).assertCancellable()).doesNotThrowAnyException();
    }

    @Test
    void aPaidOrderIsNotCancellable() {
        assertThatThrownBy(() -> orderIn(Status.PAID).assertCancellable())
                .isInstanceOf(OrderStateException.class).hasMessageContaining("PAID");
    }

    @Test
    void updateReplacesLocationAndItemsKeepingIdAndStatus() {
        var replacement = Order.create(Location.IN_STORE,
                List.of(new LineItem(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2)));

        var updated = orderIn(Status.PAYMENT_EXPECTED).update(replacement.getLocation(), replacement.getItems());

        assertThat(updated.getId()).isEqualTo(ID);
        assertThat(updated.getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
        assertThat(updated.getLocation()).isEqualTo(Location.IN_STORE);
        assertThat(updated.getItems()).isEqualTo(replacement.getItems());
    }

    @Test
    void updateFailsOnceThePaymentIsDone() {
        var replacement = Order.create(Location.IN_STORE, List.of(LATTE));

        assertThatThrownBy(() -> orderIn(Status.PAID).update(replacement.getLocation(), replacement.getItems()))
                .isInstanceOf(OrderStateException.class).hasMessageContaining("PAID");
    }
}
