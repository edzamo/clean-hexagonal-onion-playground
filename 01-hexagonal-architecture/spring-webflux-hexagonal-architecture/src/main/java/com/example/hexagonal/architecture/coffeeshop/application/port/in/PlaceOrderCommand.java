package com.example.hexagonal.architecture.coffeeshop.application.port.in;

import com.example.hexagonal.architecture.coffeeshop.domain.order.LineItem;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Location;

import java.util.List;

public record PlaceOrderCommand(Location location, List<LineItem> items) {

    public PlaceOrderCommand {
        if (location == null) {
            throw new IllegalArgumentException("location is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }
}
