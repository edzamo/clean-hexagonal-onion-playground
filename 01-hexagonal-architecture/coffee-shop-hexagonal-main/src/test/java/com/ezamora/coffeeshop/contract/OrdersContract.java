package com.ezamora.coffeeshop.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.order.LineItem;
import com.ezamora.coffeeshop.domain.order.Order;

/** Contrato del puerto {@link Orders}: lo ejecutan el fake en memoria y el adaptador JPA. */
@Transactional
public abstract class OrdersContract {

    protected static final LineItem LATTE = new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 1);

    protected abstract Orders orders();

    /** Fuerza la escritura y vacía cualquier caché para que las lecturas vayan al almacén. */
    protected void flushAndClear() {
    }

    protected static void assertSameContent(Order actual, Order expected) {
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getLocation()).isEqualTo(expected.getLocation());
        assertThat(actual.getItems()).containsExactlyElementsOf(expected.getItems());
    }

    private Order newOrder() {
        return Order.create(Location.TAKE_AWAY, List.of(LATTE));
    }

    @Test
    void saveReturnsTheStoredOrder() {
        var order = newOrder();

        assertSameContent(orders().save(order), order);
    }

    @Test
    void aSavedOrderCanBeFoundById() {
        var order = newOrder();
        orders().save(order);
        flushAndClear();

        assertSameContent(orders().findOrderById(order.getId()), order);
    }

    @Test
    void findingAMissingOrderFailsWithOrderNotFound() {
        assertThatThrownBy(() -> orders().findOrderById(UUID.randomUUID())).isInstanceOf(OrderNotFound.class);
    }

    @Test
    void savingAgainUpdatesTheStatus() {
        var order = newOrder();
        orders().save(order);
        flushAndClear();

        orders().save(order.pay());
        flushAndClear();

        assertThat(orders().findOrderById(order.getId()).getStatus()).isEqualTo(Status.PAID);
    }

    @Test
    void savingAgainReplacesTheItemsWithoutDuplicates() {
        var order = newOrder();
        orders().save(order);
        flushAndClear();
        var replacement = order.update(Location.IN_STORE, List.of(
                new LineItem(Drink.ESPRESSO, Milk.SOY, Size.SMALL, 2),
                new LineItem(Drink.CAPPUCCINO, Milk.SKIMMED, Size.MEDIUM, 1)));

        orders().save(replacement);
        flushAndClear();

        assertSameContent(orders().findOrderById(order.getId()), replacement);
    }

    @Test
    void deleteRemovesTheOrder() {
        var order = newOrder();
        orders().save(order);
        flushAndClear();

        orders().deleteById(order.getId());
        flushAndClear();

        assertThatThrownBy(() -> orders().findOrderById(order.getId())).isInstanceOf(OrderNotFound.class);
    }

    @Test
    void deletingAMissingOrderFailsWithOrderNotFound() {
        assertThatThrownBy(() -> orders().deleteById(UUID.randomUUID())).isInstanceOf(OrderNotFound.class);
    }
}
