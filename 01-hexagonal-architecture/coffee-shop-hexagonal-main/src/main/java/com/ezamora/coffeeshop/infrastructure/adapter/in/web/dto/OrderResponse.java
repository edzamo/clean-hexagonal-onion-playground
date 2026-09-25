package com.ezamora.coffeeshop.infrastructure.adapter.in.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.ezamora.coffeeshop.domain.model.enums.Location;
import com.ezamora.coffeeshop.domain.model.enums.Status;
import com.ezamora.coffeeshop.domain.model.order.Order;

public record OrderResponse(UUID id, Status status, Location location, List<LineItemResponse> items, BigDecimal cost) {

    public static OrderResponse fromDomain(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getLocation(),
                order.getItems().stream().map(LineItemResponse::fromDomain).toList(),
                order.getCost());
    }
}
