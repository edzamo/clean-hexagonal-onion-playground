package com.example.hexagonal.architecture.coffeeshop.application.service;

import com.example.hexagonal.architecture.coffeeshop.application.port.in.PlaceOrderCommand;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.SaveOrderPort;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Drink;
import com.example.hexagonal.architecture.coffeeshop.domain.order.LineItem;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Location;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Milk;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Size;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceOrderServiceTest {

    @Mock
    private SaveOrderPort saveOrderPort;

    @Test
    void placesANewOrderAndSavesIt() {
        PlaceOrderService service = new PlaceOrderService(saveOrderPort);
        PlaceOrderCommand command = new PlaceOrderCommand(
                Location.TAKE_AWAY, List.of(new LineItem(Drink.LATTE, Milk.SOY, Size.MEDIUM, 2)));
        when(saveOrderPort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.place(command))
                .expectNextMatches(order -> order.getLocation() == Location.TAKE_AWAY)
                .verifyComplete();

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(saveOrderPort).save(captor.capture());
        assertThat(captor.getValue().getItems()).hasSize(1);
    }
}
