package com.example.hexagonal.architecture.coffeeshop.application.port.out;

import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoadOrderPort {

    Mono<Order> loadById(UUID orderId);
}
