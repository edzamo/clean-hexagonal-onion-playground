package com.example.hexagonal.architecture.coffeeshop.application.service;

import com.example.hexagonal.architecture.coffeeshop.application.port.in.PlaceOrderCommand;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.PlaceOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.SaveOrderPort;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PlaceOrderService implements PlaceOrderUseCase {

    private final SaveOrderPort saveOrderPort;

    @Override
    public Mono<Order> place(PlaceOrderCommand command) {
        return Mono.defer(() -> saveOrderPort.save(new Order(command.location(), command.items())));
    }
}
