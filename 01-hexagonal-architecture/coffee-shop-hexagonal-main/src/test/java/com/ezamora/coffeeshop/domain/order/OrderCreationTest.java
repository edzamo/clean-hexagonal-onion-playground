package com.ezamora.coffeeshop.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;

class OrderCreationTest {

    private static final LineItem LATTE = new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1);

    @Test
    void createGeneratesIdAndStartsWaitingForPayment() {
        var order = Order.create(Location.TAKE_AWAY, List.of(LATTE));

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(Status.PAYMENT_EXPECTED);
        assertThat(order.getLocation()).isEqualTo(Location.TAKE_AWAY);
        assertThat(order.getItems()).containsExactly(LATTE);
    }

    @Test
    void createWithExplicitIdKeepsIt() {
        var id = UUID.randomUUID();

        assertThat(Order.create(id, Location.IN_STORE, List.of(LATTE)).getId()).isEqualTo(id);
    }

    @Test
    void rehydrateRestoresGivenState() {
        var id = UUID.randomUUID();

        var order = Order.rehydrate(id, Location.IN_STORE, List.of(LATTE), Status.READY);

        assertThat(order.getId()).isEqualTo(id);
        assertThat(order.getStatus()).isEqualTo(Status.READY);
    }

    @Test
    void costIsSumOfLineItems() {
        var order = Order.create(Location.TAKE_AWAY, List.of(LATTE,
                new LineItem(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2)));

        assertThat(order.getCost()).isEqualByComparingTo(new BigDecimal("13.0"));
    }

    @Test
    void rejectsNullLocation() {
        assertThatThrownBy(() -> Order.create(null, List.of(LATTE)))
                .isInstanceOf(InvalidOrderException.class).hasMessageContaining("location");
    }

    @Test
    void rejectsNullOrEmptyItems() {
        assertThatThrownBy(() -> Order.create(Location.TAKE_AWAY, null))
                .isInstanceOf(InvalidOrderException.class);
        assertThatThrownBy(() -> Order.create(Location.TAKE_AWAY, List.of()))
                .isInstanceOf(InvalidOrderException.class).hasMessageContaining("item");
    }

    @Test
    void itemsAreDefensivelyCopied() {
        var source = new ArrayList<>(List.of(LATTE));
        var order = Order.create(Location.TAKE_AWAY, source);

        source.clear();

        assertThat(order.getItems()).containsExactly(LATTE);
        assertThatThrownBy(() -> order.getItems().add(LATTE)).isInstanceOf(UnsupportedOperationException.class);
    }
}
