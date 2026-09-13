package com.example.hexagonal.architecture.coffeeshop.application.port.in;

import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import reactor.core.publisher.Mono;

public interface PlaceOrderUseCase {

    Mono<Order> place(PlaceOrderCommand command);
}
