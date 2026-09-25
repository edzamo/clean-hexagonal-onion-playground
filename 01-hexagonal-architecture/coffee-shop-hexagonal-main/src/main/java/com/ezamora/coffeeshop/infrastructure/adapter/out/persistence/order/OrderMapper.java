package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order;

import java.util.List;

import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;
import com.ezamora.coffeeshop.domain.order.LineItem;
import com.ezamora.coffeeshop.domain.order.Order;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.DrinkJpa;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.MilkJpa;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderItemJpaEntity;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderJpaEntity;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderLocation;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.OrderStatus;
import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity.SizeJpa;
import com.ezamora.coffeeshop.infrastructure.error.PersistenceDataCorruptedException;

/**
 * Mapper biyectivo entre el {@link Order} de dominio y {@link OrderJpaEntity}.
 * Los enums se mapean por nombre (uno a uno); un test verifica que son exhaustivos.
 * Una fila que viola invariantes del dominio (p. ej. sin ítems) lanza PersistenceDataCorruptedException;
 * la fecha de creación la fija el adaptador, no el mapper.
 */
public final class OrderMapper {

    private OrderMapper() {
    }

    public static Order toDomain(OrderJpaEntity entity) {
        try {
            return Order.rehydrate(
                    entity.getUuid(),
                    Location.valueOf(entity.getLocation().name()),
                    toDomainItems(entity.getItems()),
                    Status.valueOf(entity.getStatus().name()));
        } catch (InvalidOrderException e) {
            throw new PersistenceDataCorruptedException("Stored order " + entity.getUuid() + " is corrupted", e);
        }
    }

    private static List<LineItem> toDomainItems(List<OrderItemJpaEntity> items) {
        return items == null ? List.of() : items.stream().map(OrderMapper::toDomain).toList();
    }

    public static OrderJpaEntity toEntity(Order order) {
        var entity = OrderJpaEntity.builder()
                .uuid(order.getId())
                .totalAmount(order.getCost())
                .location(OrderLocation.valueOf(order.getLocation().name()))
                .status(OrderStatus.valueOf(order.getStatus().name()))
                .build();
        entity.setItems(order.getItems().stream().map(OrderMapper::toEntity).toList());
        return entity;
    }

    private static LineItem toDomain(OrderItemJpaEntity item) {
        return new LineItem(
                Drink.valueOf(item.getDrink().name()),
                Milk.valueOf(item.getMilk().name()),
                Size.valueOf(item.getSize().name()),
                item.getQuantity());
    }

    private static OrderItemJpaEntity toEntity(LineItem item) {
        return OrderItemJpaEntity.builder()
                .drink(DrinkJpa.valueOf(item.drink().name()))
                .milk(MilkJpa.valueOf(item.milk().name()))
                .size(SizeJpa.valueOf(item.size().name()))
                .quantity(item.quantity())
                .build();
    }
}
