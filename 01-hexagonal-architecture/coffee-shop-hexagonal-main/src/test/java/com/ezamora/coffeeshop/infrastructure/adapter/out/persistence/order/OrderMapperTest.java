package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;
import com.ezamora.coffeeshop.domain.order.LineItem;
import com.ezamora.coffeeshop.domain.order.Order;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.DrinkJpa;
import com.ezamora.coffeeshop.infrastructure.error.PersistenceDataCorruptedException;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.MilkJpa;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderLocation;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderStatus;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.SizeJpa;

class OrderMapperTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    private static Order order(Status status, Location location, LineItem... items) {
        return Order.rehydrate(ID, location, List.of(items), status);
    }

    private static void assertRoundTrip(Order original) {
        var restored = OrderMapper.toDomain(OrderMapper.toEntity(original));

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getLocation()).isEqualTo(original.getLocation());
        assertThat(restored.getItems()).isEqualTo(original.getItems());
    }

    private static Set<String> names(Class<? extends Enum<?>> type) {
        return Arrays.stream(type.getEnumConstants()).map(Enum::name).collect(Collectors.toSet());
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void everyStatusSurvivesARoundTrip(Status status) {
        assertRoundTrip(order(status, Location.IN_STORE, new LineItem(Drink.LATTE, Milk.WHOLE, Size.SMALL, 1)));
    }

    @ParameterizedTest
    @EnumSource(Drink.class)
    void everyDrinkSurvivesARoundTrip(Drink drink) {
        assertRoundTrip(order(Status.PAID, Location.IN_STORE, new LineItem(drink, Milk.WHOLE, Size.SMALL, 1)));
    }

    @ParameterizedTest
    @EnumSource(Milk.class)
    void everyMilkSurvivesARoundTrip(Milk milk) {
        assertRoundTrip(order(Status.PAID, Location.IN_STORE, new LineItem(Drink.LATTE, milk, Size.SMALL, 1)));
    }

    @ParameterizedTest
    @EnumSource(Size.class)
    void everySizeSurvivesARoundTrip(Size size) {
        assertRoundTrip(order(Status.PAID, Location.IN_STORE, new LineItem(Drink.LATTE, Milk.SOY, size, 3)));
    }

    @ParameterizedTest
    @EnumSource(Location.class)
    void everyLocationSurvivesARoundTrip(Location location) {
        assertRoundTrip(order(Status.PAID, location, new LineItem(Drink.LATTE, Milk.SOY, Size.LARGE, 1)));
    }

    @Test
    void persistenceEnumsAreExhaustiveWithRespectToDomainEnums() {
        assertThat(names(OrderStatus.class)).isEqualTo(names(Status.class));
        assertThat(names(OrderLocation.class)).isEqualTo(names(Location.class));
        assertThat(names(DrinkJpa.class)).isEqualTo(names(Drink.class));
        assertThat(names(MilkJpa.class)).isEqualTo(names(Milk.class));
        assertThat(names(SizeJpa.class)).isEqualTo(names(Size.class));
    }

    private static OrderJpaEntity anEntity() {
        return OrderMapper.toEntity(order(Status.PAID, Location.IN_STORE,
                new LineItem(Drink.LATTE, Milk.WHOLE, Size.SMALL, 1)));
    }

    @Test
    void toEntityKeepsTheDomainIdAndNeverGeneratesOne() {
        assertThat(anEntity().getUuid()).isEqualTo(ID);
    }

    @Test
    void toEntityMapsEveryLineItem() {
        assertThat(anEntity().getItems()).hasSize(1);
    }

    @Test
    void toEntityLeavesTheCreationDateToTheAdapter() {
        assertThat(anEntity().getOrderDate()).isNull();
    }

    @Test
    void corruptedRowWithoutItemsIsReportedAsPersistenceCorruptionNotAsAClientError() {
        var corrupted = OrderJpaEntity.builder().uuid(ID).status(OrderStatus.PAID)
                .location(OrderLocation.IN_STORE).build();

        assertThatThrownBy(() -> OrderMapper.toDomain(corrupted))
                .isInstanceOf(PersistenceDataCorruptedException.class)
                .hasMessageContaining(ID.toString())
                .hasCauseInstanceOf(InvalidOrderException.class);
    }
}
