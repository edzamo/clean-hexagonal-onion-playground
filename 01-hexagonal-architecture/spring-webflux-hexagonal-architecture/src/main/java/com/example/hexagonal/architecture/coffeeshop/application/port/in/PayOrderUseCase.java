package com.example.hexagonal.architecture.coffeeshop.application.port.in;

import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PayOrderUseCase {

    Mono<Order> pay(UUID orderId);
}
