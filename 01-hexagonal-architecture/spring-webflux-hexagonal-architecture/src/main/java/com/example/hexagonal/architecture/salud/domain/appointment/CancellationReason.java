package com.example.hexagonal.architecture.salud.domain.appointment;

import java.time.LocalDateTime;

public record CancellationReason(String description, LocalDateTime cancelledAt) {

    public CancellationReason {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        if (cancelledAt == null) {
            throw new IllegalArgumentException("cancelledAt is required");
        }
    }

    public static CancellationReason now(String description) {
        return new CancellationReason(description, LocalDateTime.now());
    }
}
