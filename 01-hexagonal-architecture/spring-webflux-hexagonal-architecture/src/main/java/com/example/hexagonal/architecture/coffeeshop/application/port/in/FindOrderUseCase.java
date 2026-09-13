package com.example.hexagonal.architecture.coffeeshop.application.port.in;

import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FindOrderUseCase {

    Mono<Order> findById(UUID orderId);
}
