package com.example.hexagonal.architecture.coffeeshop.application.service;

import com.example.hexagonal.architecture.coffeeshop.application.port.in.PayOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.LoadOrderPort;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.SaveOrderPort;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import com.example.hexagonal.architecture.coffeeshop.domain.order.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PayOrderService implements PayOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;

    @Override
    public Mono<Order> pay(UUID orderId) {
        return loadOrderPort.loadById(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .map(Order::pay)
                .flatMap(saveOrderPort::save);
    }
}
