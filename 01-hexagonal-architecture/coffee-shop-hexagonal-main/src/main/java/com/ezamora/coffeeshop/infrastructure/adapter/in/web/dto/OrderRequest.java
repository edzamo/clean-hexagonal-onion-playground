package com.ezamora.coffeeshop.infrastructure.adapter.in.web.dto;

import java.util.List;

import com.ezamora.coffeeshop.domain.enums.Location;
import com.ezamora.coffeeshop.domain.order.Order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderRequest(@NotNull Location location, @NotEmpty @Size(max = 50) List<@Valid @NotNull LineItemRequest> items) {

    public Order toDomain() {
        return Order.create(location, items.stream().map(LineItemRequest::toDomain).toList());
    }
}
