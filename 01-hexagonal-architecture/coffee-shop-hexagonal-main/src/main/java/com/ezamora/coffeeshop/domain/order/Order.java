package com.ezamora.coffeeshop.domain.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.enums.Status;
import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;
import com.ezamora.coffeeshop.domain.exception.OrderStateException;

/** Pedido inmutable: cada transición devuelve un nuevo {@code Order}. */
public final class Order {

    private final UUID id;
    private final Location location;
    private final List<LineItem> items;
    private final Status status;

    private Order(UUID id, Location location, List<LineItem> items, Status status) {
        if (id == null) {
            throw new InvalidOrderException("Order id must not be null");
        }
        if (location == null) {
            throw new InvalidOrderException("Order location must not be null");
        }
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must have at least one item");
        }
        if (status == null) {
            throw new InvalidOrderException("Order status must not be null");
        }
        this.id = id;
        this.location = location;
        this.items = List.copyOf(items);
        this.status = status;
    }

    /** Crea un pedido nuevo con id generado, pendiente de pago. */
    public static Order create(Location location, List<LineItem> items) {
        return create(UUID.randomUUID(), location, items);
    }

    /** Crea un pedido nuevo con id dado (tests deterministas), pendiente de pago. */
    public static Order create(UUID id, Location location, List<LineItem> items) {
        return new Order(id, location, items, Status.PAYMENT_EXPECTED);
    }

    /** Reconstruye un pedido persistido con su estado (uso del mapper de salida). */
    public static Order rehydrate(UUID id, Location location, List<LineItem> items, Status status) {
        return new Order(id, location, items, status);
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public List<LineItem> getItems() {
        return items;
    }

    public Status getStatus() {
        return status;
    }

    public BigDecimal getCost() {
        return items.stream().map(LineItem::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Sustituye ubicación e ítems; solo mientras se espera el pago. */
    public Order update(Location newLocation, List<LineItem> newItems) {
        requireStatus(Status.PAYMENT_EXPECTED, "updated");
        return new Order(id, newLocation, newItems, status);
    }

    /** Valida que se pueda cancelar; la eliminación la orquesta la aplicación. */
    public void assertCancellable() {
        requireStatus(Status.PAYMENT_EXPECTED, "cancelled");
    }

    public Order pay() {
        return transition(Status.PAYMENT_EXPECTED, Status.PAID, "paid");
    }

    public Order startPreparing() {
        return transition(Status.PAID, Status.PREPARING, "started preparing");
    }

    public Order finishPreparing() {
        return transition(Status.PREPARING, Status.READY, "finished preparing");
    }

    public Order take() {
        return transition(Status.READY, Status.TAKEN, "taken");
    }

    private Order transition(Status expected, Status target, String action) {
        requireStatus(expected, action);
        return new Order(id, location, items, target);
    }

    private void requireStatus(Status expected, String action) {
        if (status != expected) {
            throw new OrderStateException("Order " + id + " cannot be " + action
                    + ": status is " + status + " but " + expected + " is required");
        }
    }
}
