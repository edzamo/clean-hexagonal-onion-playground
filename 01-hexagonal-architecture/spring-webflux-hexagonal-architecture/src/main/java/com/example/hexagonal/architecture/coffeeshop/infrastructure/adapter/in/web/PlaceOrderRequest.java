package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.coffeeshop.application.port.in.PlaceOrderCommand;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceOrderRequest(@NotNull Location location, @NotEmpty @Valid List<LineItemRequest> items) {

    public PlaceOrderCommand toCommand() {
        return new PlaceOrderCommand(location, items.stream().map(LineItemRequest::toLineItem).toList());
    }
}
