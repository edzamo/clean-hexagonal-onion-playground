package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence;

import com.example.hexagonal.architecture.coffeeshop.application.port.out.LoadOrderPort;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.SaveOrderPort;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {

    private final Map<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public Mono<Order> loadById(UUID orderId) {
        return Mono.justOrEmpty(store.get(orderId));
    }

    @Override
    public Mono<Order> save(Order order) {
        return Mono.fromCallable(() -> {
            UUID id = order.getId() != null ? order.getId() : UUID.randomUUID();
            Order toStore = new Order(id, order.getLocation(), order.getItems(), order.getStatus());
            store.put(id, toStore);
            return toStore;
        });
    }
}
