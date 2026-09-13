package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence.mapper;

import com.example.hexagonal.architecture.coffeeshop.domain.order.LineItem;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Location;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Order;
import com.example.hexagonal.architecture.coffeeshop.domain.order.Status;
import com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OrderPersistenceMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public OrderEntity toEntity(Order order) {
        UUID id = order.getId() != null ? order.getId() : UUID.randomUUID();
        return new OrderEntity(
                id,
                order.getLocation().name(),
                objectMapper.writeValueAsString(order.getItems()),
                order.getStatus().name());
    }

    @SneakyThrows
    public Order toDomain(OrderEntity entity) {
        List<LineItem> items = objectMapper.readValue(entity.itemsJson(), new TypeReference<List<LineItem>>() {
        });
        return new Order(
                entity.id(),
                Location.valueOf(entity.location()),
                items,
                Status.valueOf(entity.status()));
    }
}
