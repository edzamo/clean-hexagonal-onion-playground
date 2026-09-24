package com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.ezamora.coffeeshop.infrastructure.adapter.out.persistence.common.UUIDConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

/**
 * Pago persistido. Referencia la orden por su UUID público ({@code order_uuid}, FK a nivel de BD
 * sobre orders.uuid) y no por una asociación JPA: así persistence.payment no depende de
 * persistence.order. Nunca guarda el PAN: solo los últimos 4 dígitos.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "order_uuid", nullable = false, unique = true, length = 36)
    @Convert(converter = UUIDConverter.class)
    private UUID orderUuid;

    @Column(nullable = false, length = 4)
    private String last4;

    @ToString.Exclude
    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
}
