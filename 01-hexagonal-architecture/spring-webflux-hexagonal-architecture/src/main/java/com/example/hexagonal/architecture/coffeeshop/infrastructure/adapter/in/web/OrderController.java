package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.coffeeshop.application.port.in.FindOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.MarkOrderReadyUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.PayOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.PlaceOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.PrepareOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.TakeOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.domain.order.OrderNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final PayOrderUseCase payOrderUseCase;
    private final PrepareOrderUseCase prepareOrderUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    private final TakeOrderUseCase takeOrderUseCase;
    private final FindOrderUseCase findOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        return placeOrderUseCase.place(request.toCommand())
                .map(OrderResponse::from);
    }

    @GetMapping("/{id}")
    public Mono<OrderResponse> findById(@PathVariable UUID id) {
        return findOrderUseCase.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .map(OrderResponse::from);
    }

    @PostMapping("/{id}/pay")
    public Mono<OrderResponse> pay(@PathVariable UUID id) {
        return payOrderUseCase.pay(id)
                .map(OrderResponse::from);
    }

    @PostMapping("/{id}/prepare")
    public Mono<OrderResponse> prepare(@PathVariable UUID id) {
        return prepareOrderUseCase.prepare(id)
                .map(OrderResponse::from);
    }

    @PostMapping("/{id}/ready")
    public Mono<OrderResponse> markReady(@PathVariable UUID id) {
        return markOrderReadyUseCase.markReady(id)
                .map(OrderResponse::from);
    }

    @PostMapping("/{id}/take")
    public Mono<OrderResponse> take(@PathVariable UUID id) {
        return takeOrderUseCase.take(id)
                .map(OrderResponse::from);
    }
}
