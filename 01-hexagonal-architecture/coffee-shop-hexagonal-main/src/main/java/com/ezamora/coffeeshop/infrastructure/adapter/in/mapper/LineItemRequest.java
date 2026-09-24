package com.ezamora.coffeeshop.infrastructure.adapter.in.mapper;

import com.ezamora.coffeeshop.domain.model.enums.Drink;
import com.ezamora.coffeeshop.domain.model.enums.Milk;
import com.ezamora.coffeeshop.domain.model.enums.Size;
import com.ezamora.coffeeshop.domain.model.order.LineItem;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineItemRequest(@NotNull Drink drink, @NotNull Milk milk, @NotNull Size size, @NotNull @Positive @Max(99) Integer quantity) {

    public LineItem toDomain() {
        return new LineItem(drink, milk, size, quantity);
    }
}
