package com.example.hexagonal.architecture.domain.appointment;

import java.util.UUID;

public record PractitionerId(UUID value) {

    public PractitionerId {
        if (value == null) {
            throw new IllegalArgumentException("PractitionerId cannot be null");
        }
    }

    public static PractitionerId newId() {
        return new PractitionerId(UUID.randomUUID());
    }
}
