package com.example.hexagonal.architecture.coffeeshop.application.port.out;

import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import reactor.core.publisher.Mono;

public interface SaveOrderPort {

    Mono<Order> save(Order order);
}
