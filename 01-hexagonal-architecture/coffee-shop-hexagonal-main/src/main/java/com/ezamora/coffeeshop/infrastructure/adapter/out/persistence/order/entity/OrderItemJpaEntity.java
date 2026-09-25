package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.order.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.NoArgsConstructor;

@Getter
@Setter
@EqualsAndHashCode(exclude = { "id", "order" })
@ToString(exclude = "order")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DrinkJpa drink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilkJpa milk;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SizeJpa size;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    // Custom setter to maintain bidirectional consistency
    public void setOrder(OrderJpaEntity order) {
        this.order = order;
    }
}