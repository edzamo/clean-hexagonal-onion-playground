package com.example.hexagonal.architecture.coffeeshop.domain.order;

import java.util.List;
import java.util.UUID;

public class Order {

    private UUID id;
    private final Location location;
    private final List<LineItem> items;
    private Status status = Status.PAYMENT_EXPECTED;

    public Order(Location location, List<LineItem> items) {
        if (location == null || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("location and items are required");
        }
        this.location = location;
        this.items = List.copyOf(items);
    }

    public Order(UUID id, Location location, List<LineItem> items, Status status) {
        this.id = id;
        this.location = location;
        this.items = List.copyOf(items);
        this.status = status;
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

    public Order pay() {
        if (status != Status.PAYMENT_EXPECTED) {
            throw new InvalidOrderTransitionException("Only a payment-expected order can be paid");
        }
        status = Status.PAID;
        return this;
    }

    public Order prepare() {
        if (status != Status.PAID) {
            throw new InvalidOrderTransitionException("Only a paid order can start preparing");
        }
        status = Status.PREPARING;
        return this;
    }

    public Order markReady() {
        if (status != Status.PREPARING) {
            throw new InvalidOrderTransitionException("Only a preparing order can be marked ready");
        }
        status = Status.READY;
        return this;
    }

    public Order take() {
        if (status != Status.READY) {
            throw new InvalidOrderTransitionException("Only a ready order can be taken");
        }
        status = Status.TAKEN;
        return this;
    }
}
