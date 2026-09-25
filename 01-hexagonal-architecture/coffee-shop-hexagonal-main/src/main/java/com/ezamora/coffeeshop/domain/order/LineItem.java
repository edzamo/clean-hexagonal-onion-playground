package com.ezamora.coffeeshop.domain.order;

import java.math.BigDecimal;

import com.ezamora.coffeeshop.domain.enums.Drink;
import com.ezamora.coffeeshop.domain.enums.Milk;
import com.ezamora.coffeeshop.domain.enums.Size;
import com.ezamora.coffeeshop.domain.exception.InvalidOrderException;

/** Línea de pedido: bebida, leche, tamaño y cantidad (> 0). */
public record LineItem(Drink drink, Milk milk, Size size, int quantity) {

    private static final BigDecimal SMALL_MEDIUM_PRICE = BigDecimal.valueOf(4.0);
    private static final BigDecimal LARGE_PRICE = BigDecimal.valueOf(5.0);

    public LineItem {
        if (drink == null) {
            throw new InvalidOrderException("Line item drink must not be null");
        }
        if (milk == null) {
            throw new InvalidOrderException("Line item milk must not be null");
        }
        if (size == null) {
            throw new InvalidOrderException("Line item size must not be null");
        }
        if (quantity <= 0) {
            throw new InvalidOrderException("Line item quantity must be greater than zero but was " + quantity);
        }
    }

    // Precio simplificado: SMALL y MEDIUM cuestan 4.0, LARGE 5.0 por unidad.
    BigDecimal getCost() {
        var unitPrice = size == Size.LARGE ? LARGE_PRICE : SMALL_MEDIUM_PRICE;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
