package com.example.hexagonal.architecture.coffeeshop.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("orders")
public record OrderEntity(
        @Id @Column("id") UUID id,
        @Column("location") String location,
        @Column("items") String itemsJson,
        @Column("status") String status) {
}
