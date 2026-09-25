package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.contract.OrdersContract;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.order.Order;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderStatus;

/** Contrato del puerto {@code Orders} (heredado) más las garantías propias de JPA, con H2. */
@DataJpaTest
@ActiveProfiles("test")
@Import({ OrderServiceAdapter.class, OrderServiceAdapterTest.FixedClockConfig.class })
class OrderServiceAdapterTest extends OrdersContract {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2025-06-15T10:00:00Z"), ZoneOffset.UTC);

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return FIXED_CLOCK;
        }
    }

    @Autowired
    private OrderServiceAdapter adapter;
    @Autowired
    private OrderRepository repository;
    @Autowired
    private TestEntityManager em;

    @Override
    protected Orders orders() {
        return adapter;
    }

    @Override
    protected void flushAndClear() {
        em.flush();
        em.clear();
    }

    private Order savedOrder() {
        var order = Order.create(Location.TAKE_AWAY, List.of(LATTE));
        adapter.save(order);
        flushAndClear();
        return order;
    }

    @Test
    void savingANewOrderInsertsExactlyOneRow() {
        savedOrder();

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void savingANewOrderStampsItsCreationDateFromTheInjectedClock() {
        var order = savedOrder();

        assertThat(repository.findByUuid(order.getId()).orElseThrow().getOrderDate())
                .isEqualTo(LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    void updatingKeepsTheInternalId() {
        var order = savedOrder();
        var internalId = repository.findByUuid(order.getId()).orElseThrow().getId();
        em.clear();

        adapter.save(order.pay());
        flushAndClear();

        assertThat(repository.findByUuid(order.getId()).orElseThrow().getId()).isEqualTo(internalId);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void updatingKeepsTheOriginalCreationDate() {
        var order = savedOrder();
        var orderDate = repository.findByUuid(order.getId()).orElseThrow().getOrderDate();
        em.clear();

        adapter.save(order.pay());
        flushAndClear();

        assertThat(repository.findByUuid(order.getId()).orElseThrow().getOrderDate()).isEqualTo(orderDate);
    }

    @Test
    void staleConcurrentUpdateIsRejectedByOptimisticLocking() {
        var order = savedOrder();
        var stale = repository.findByUuid(order.getId()).orElseThrow();
        em.detach(stale);
        adapter.save(order.pay());
        flushAndClear();

        stale.setStatus(OrderStatus.PAID);

        assertThat(stale.getVersion()).isNotNull();
        assertThatThrownBy(() -> repository.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
