package com.example.hexagonal.architecture.coffeeshop.application.service;

import com.example.hexagonal.architecture.coffeeshop.application.port.out.LoadOrderPort;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.SaveOrderPort;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Drink;
import com.example.hexagonal.architecture.coffeeshop.domain.order.LineItem;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Location;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Milk;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import com.example.hexagonal.architecture.coffeeshop.domain.order.OrderNotFoundException;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Size;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayOrderServiceTest {

    @Mock
    private LoadOrderPort loadOrderPort;

    @Mock
    private SaveOrderPort saveOrderPort;

    private Order paymentExpectedOrder(UUID id) {
        return new Order(id, Location.TAKE_AWAY,
                List.of(new LineItem(Drink.LATTE, Milk.SOY, Size.MEDIUM, 1)), Status.PAYMENT_EXPECTED);
    }

    @Test
    void paysAnExistingOrder() {
        UUID id = UUID.randomUUID();
        when(loadOrderPort.loadById(id)).thenReturn(Mono.just(paymentExpectedOrder(id)));
        when(saveOrderPort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        PayOrderService service = new PayOrderService(loadOrderPort, saveOrderPort);

        StepVerifier.create(service.pay(id))
                .expectNextMatches(order -> order.getStatus() == Status.PAID)
                .verifyComplete();
    }

    @Test
    void failsWithNotFoundWhenOrderDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(loadOrderPort.loadById(id)).thenReturn(Mono.empty());

        PayOrderService service = new PayOrderService(loadOrderPort, saveOrderPort);

        StepVerifier.create(service.pay(id))
                .expectError(OrderNotFoundException.class)
                .verify();
    }
}
