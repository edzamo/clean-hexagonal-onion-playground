package com.ezamora.coffeeshop.infrastructure.adapter.in.web.dto;

import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.order.LineItem;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineItemRequest(@NotNull Drink drink, @NotNull Milk milk, @NotNull Size size, @NotNull @Positive @Max(99) Integer quantity) {

    public LineItem toDomain() {
        return new LineItem(drink, milk, size, quantity);
    }
}
