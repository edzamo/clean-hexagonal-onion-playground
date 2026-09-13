package com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("appointments")
public record AppointmentEntity(
        @Id @Column("id") UUID id,
        @Column("patient_id") UUID patientId,
        @Column("practitioner_id") UUID practitionerId,
        @Column("start_time") LocalDateTime startTime,
        @Column("end_time") LocalDateTime endTime,
        @Column("reason") String reason,
        @Column("status") String status,
        @Column("cancellation_description") String cancellationDescription,
        @Column("cancelled_at") LocalDateTime cancelledAt) {
}
