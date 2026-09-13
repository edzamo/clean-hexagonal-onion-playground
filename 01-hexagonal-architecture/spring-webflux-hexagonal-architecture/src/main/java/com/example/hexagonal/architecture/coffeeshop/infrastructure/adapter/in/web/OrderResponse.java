package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.coffeeshop.domain.order.LineItem;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Location;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Status;

import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, Location location, List<LineItem> items, Status status) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getLocation(), order.getItems(), order.getStatus());
    }
}
