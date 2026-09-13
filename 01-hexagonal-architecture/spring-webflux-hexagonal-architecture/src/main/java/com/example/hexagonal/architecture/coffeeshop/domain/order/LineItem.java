package com.example.hexagonal.architecture.coffeeshop.domain.order;

public record LineItem(Drink drink, Milk milk, Size size, int quantity) {

}
