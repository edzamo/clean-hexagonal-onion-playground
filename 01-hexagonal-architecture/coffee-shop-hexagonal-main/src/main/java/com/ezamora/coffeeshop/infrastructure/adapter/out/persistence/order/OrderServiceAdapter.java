package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ezamora.coffeeshop.application.out.OrderNotFound;
import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.domain.model.order.Order;

import lombok.RequiredArgsConstructor;

/** Adaptador de salida del puerto {@link Orders} sobre JPA. */
@Component
@RequiredArgsConstructor
public class OrderServiceAdapter implements Orders {

    private final OrderRepository orderRepository;

    @Override
    public Order findOrderById(UUID orderId) throws OrderNotFound {
        return orderRepository.findByUuid(orderId)
                .map(OrderMapper::toDomain)
                .orElseThrow(() -> new OrderNotFound("Order not found with id: " + orderId));
    }

    @Override
    public Order save(Order order) {
        var newState = OrderMapper.toEntity(order);

        // Si ya existe se actualiza la entidad gestionada: conserva id interno y fecha de creación.
        var entity = orderRepository.findByUuid(order.getId()).map(existing -> {
            existing.setStatus(newState.getStatus());
            existing.setLocation(newState.getLocation());
            existing.setTotalAmount(newState.getTotalAmount());
            existing.setItems(newState.getItems());
            return existing;
        }).orElseGet(() -> {
            newState.setOrderDate(LocalDateTime.now());
            return newState;
        });

        return OrderMapper.toDomain(orderRepository.save(entity));
    }

    @Override
    public void deleteById(UUID orderId) {
        // El repositorio trabaja con la PK interna (Long); se localiza primero por el UUID público.
        var entity = orderRepository.findByUuid(orderId)
                .orElseThrow(() -> new OrderNotFound("Cannot delete an order that does not exist: " + orderId));
        orderRepository.delete(entity);
    }
}
