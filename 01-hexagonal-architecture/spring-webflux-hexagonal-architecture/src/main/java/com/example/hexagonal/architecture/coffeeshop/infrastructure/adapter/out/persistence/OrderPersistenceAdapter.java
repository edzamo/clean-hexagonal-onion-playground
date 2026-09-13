package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence;

import com.example.hexagonal.architecture.coffeeshop.application.port.out.LoadOrderPort;
import com.example.hexagonal.architecture.coffeeshop.application.port.out.SaveOrderPort;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence.mapper.OrderPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class OrderPersistenceAdapter implements LoadOrderPort, SaveOrderPort {

    private final SpringDataOrderRepository repository;
    private final R2dbcEntityTemplate entityTemplate;
    private final OrderPersistenceMapper mapper;

    @Override
    public Mono<Order> loadById(UUID orderId) {
        return repository.findById(orderId).map(mapper::toDomain);
    }

    @Override
    public Mono<Order> save(Order order) {
        boolean isNew = order.getId() == null;
        OrderEntity entity = mapper.toEntity(order);
        Mono<OrderEntity> persisted = isNew
                ? entityTemplate.insert(entity)
                : entityTemplate.update(entity);
        return persisted.map(mapper::toDomain);
    }
}
