package com.example.hexagonal.architecture.salud.domain.appointment;

import java.util.UUID;

public record PatientId(UUID value) {

    public PatientId {
        if (value == null) {
            throw new IllegalArgumentException("PatientId cannot be null");
        }
    }

    public static PatientId newId() {
        return new PatientId(UUID.randomUUID());
    }
}
