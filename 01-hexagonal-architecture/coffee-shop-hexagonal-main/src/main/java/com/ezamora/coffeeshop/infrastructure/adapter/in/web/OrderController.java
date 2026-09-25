package com.ezamora.coffeeshop.infrastructure.adapter.in.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.ezamora.coffeeshop.application.in.OrderingCoffee;
import com.ezamora.coffeeshop.application.in.PreparingCoffee;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.OrderRequest;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.OrderResponse;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.PayRequest;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.PaymentResponse;
import com.ezamora.coffeeshop.infrastructure.adapter.in.mapper.ReceiptResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Adaptador de entrada HTTP. No registra el cuerpo de las peticiones (contienen datos de tarjeta). */
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderingCoffee orderingCoffee;
    private final PreparingCoffee preparingCoffee;

    @PostMapping("/order")
    ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request,
            UriComponentsBuilder uriComponentsBuilder) {
        var order = orderingCoffee.placeOrder(request.toDomain());
        var location = uriComponentsBuilder.path("/order/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(OrderResponse.fromDomain(order));
    }

    @PostMapping("/order/{id}")
    ResponseEntity<OrderResponse> updateOrder(@PathVariable UUID id, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(OrderResponse.fromDomain(orderingCoffee.updateOrder(id, request.toDomain())));
    }

    @DeleteMapping("/order/{id}")
    ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        orderingCoffee.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{id}")
    ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(OrderResponse.fromDomain(orderingCoffee.findOrderById(id)));
    }

    @PostMapping("/order/{id}/pay")
    ResponseEntity<PaymentResponse> payOrder(@PathVariable UUID id, @Valid @RequestBody PayRequest request) {
        return ResponseEntity.ok(PaymentResponse.fromDomain(orderingCoffee.payOrder(id, request.toDomain())));
    }

    @GetMapping("/order/{id}/receipt")
    ResponseEntity<ReceiptResponse> readReceipt(@PathVariable UUID id) {
        return ResponseEntity.ok(ReceiptResponse.fromDomain(orderingCoffee.readReceipt(id)));
    }

    @PutMapping("/order/{id}/prepare/start")
    ResponseEntity<OrderResponse> startPreparing(@PathVariable UUID id) {
        return ResponseEntity.ok(OrderResponse.fromDomain(preparingCoffee.startPreparingOrder(id)));
    }

    @PutMapping("/order/{id}/prepare/finish")
    ResponseEntity<OrderResponse> finishPreparing(@PathVariable UUID id) {
        return ResponseEntity.ok(OrderResponse.fromDomain(preparingCoffee.finishPreparingOrder(id)));
    }

    @PutMapping("/order/{id}/take")
    ResponseEntity<OrderResponse> takeOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(OrderResponse.fromDomain(orderingCoffee.takeOrder(id)));
    }
}
