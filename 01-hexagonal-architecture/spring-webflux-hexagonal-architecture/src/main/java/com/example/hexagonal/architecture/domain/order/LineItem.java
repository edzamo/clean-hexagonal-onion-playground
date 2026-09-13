package com.example.hexagonal.architecture.domain.order;

public record LineItem(Drink drink, Milk milk, Size size, int quantity) {

}
