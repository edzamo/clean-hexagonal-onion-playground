package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.coffeeshop.application.port.in.FindOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.MarkOrderReadyUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.PayOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.PlaceOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.PrepareOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.application.port.in.TakeOrderUseCase;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Drink;
import com.example.hexagonal.architecture.coffeeshop.domain.order.LineItem;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Location;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Milk;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Size;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PlaceOrderUseCase placeOrderUseCase;
    @MockitoBean
    private PayOrderUseCase payOrderUseCase;
    @MockitoBean
    private PrepareOrderUseCase prepareOrderUseCase;
    @MockitoBean
    private MarkOrderReadyUseCase markOrderReadyUseCase;
    @MockitoBean
    private TakeOrderUseCase takeOrderUseCase;
    @MockitoBean
    private FindOrderUseCase findOrderUseCase;

    @Test
    void findByIdReturns404WhenUseCaseReturnsEmpty() {
        UUID id = UUID.randomUUID();
        when(findOrderUseCase.findById(id)).thenReturn(Mono.empty());

        webTestClient.get().uri("/orders/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void findByIdReturnsOrderWhenPresent() {
        UUID id = UUID.randomUUID();
        Order order = new Order(id, Location.TAKE_AWAY,
                List.of(new LineItem(Drink.LATTE, Milk.SOY, Size.MEDIUM, 1)), Status.PAYMENT_EXPECTED);
        when(findOrderUseCase.findById(id)).thenReturn(Mono.just(order));

        webTestClient.get().uri("/orders/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PAYMENT_EXPECTED");
    }

    @Test
    void placeWithEmptyItemsIsRejectedWithBadRequest() {
        webTestClient.post().uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"location":"IN_STORE","items":[]}
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
