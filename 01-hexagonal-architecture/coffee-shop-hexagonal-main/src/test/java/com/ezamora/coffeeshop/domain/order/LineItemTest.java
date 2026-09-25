package com.ezamora.coffeeshop.domain.model.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.ezamora.coffeeshop.domain.model.enums.Drink;
import com.ezamora.coffeeshop.domain.model.enums.Milk;
import com.ezamora.coffeeshop.domain.model.enums.Size;
import com.ezamora.coffeeshop.domain.model.exception.InvalidOrderException;

class LineItemTest {

    @Test
    void smallDrinkCostsFourPerUnit() {
        assertThat(new LineItem(Drink.LATTE, Milk.WHOLE, Size.SMALL, 2).getCost())
                .isEqualByComparingTo(new BigDecimal("8.0"));
    }

    @Test
    void mediumDrinkCostsFourPerUnit() {
        assertThat(new LineItem(Drink.LATTE, Milk.WHOLE, Size.MEDIUM, 3).getCost())
                .isEqualByComparingTo(new BigDecimal("12.0"));
    }

    @Test
    void largeDrinkCostsFivePerUnit() {
        assertThat(new LineItem(Drink.LATTE, Milk.WHOLE, Size.LARGE, 2).getCost())
                .isEqualByComparingTo(new BigDecimal("10.0"));
    }

    @Test
    void rejectsNullDrink() {
        assertThatThrownBy(() -> new LineItem(null, Milk.WHOLE, Size.SMALL, 1))
                .isInstanceOf(InvalidOrderException.class).hasMessageContaining("drink");
    }

    @Test
    void rejectsNullMilk() {
        assertThatThrownBy(() -> new LineItem(Drink.LATTE, null, Size.SMALL, 1))
                .isInstanceOf(InvalidOrderException.class).hasMessageContaining("milk");
    }

    @Test
    void rejectsNullSize() {
        assertThatThrownBy(() -> new LineItem(Drink.LATTE, Milk.WHOLE, null, 1))
                .isInstanceOf(InvalidOrderException.class).hasMessageContaining("size");
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThatThrownBy(() -> new LineItem(Drink.LATTE, Milk.WHOLE, Size.SMALL, 0))
                .isInstanceOf(InvalidOrderException.class).hasMessageContaining("quantity");
        assertThatThrownBy(() -> new LineItem(Drink.LATTE, Milk.WHOLE, Size.SMALL, -1))
                .isInstanceOf(InvalidOrderException.class);
    }
}
