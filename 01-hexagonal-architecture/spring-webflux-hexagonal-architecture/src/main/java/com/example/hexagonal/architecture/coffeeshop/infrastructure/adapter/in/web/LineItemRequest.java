package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.coffeeshop.domain.order.Drink;
import com.example.hexagonal.architecture.coffeeshop.domain.order.LineItem;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Milk;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LineItemRequest(@NotNull Drink drink, @NotNull Milk milk, @NotNull Size size,
                               @Min(1) int quantity) {

    public LineItem toLineItem() {
        return new LineItem(drink, milk, size, quantity);
    }
}
