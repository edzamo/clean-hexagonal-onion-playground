package com.example.hexagonal.architecture.coffeeshop.application.service;

import com.example.hexagonal.architecture.coffeeshop.application.port.in.FindOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.LoadOrderPort;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindOrderService implements FindOrderUseCase {

    private final LoadOrderPort loadOrderPort;

    @Override
    public Mono<Order> findById(UUID orderId) {
        return loadOrderPort.loadById(orderId);
    }
}
