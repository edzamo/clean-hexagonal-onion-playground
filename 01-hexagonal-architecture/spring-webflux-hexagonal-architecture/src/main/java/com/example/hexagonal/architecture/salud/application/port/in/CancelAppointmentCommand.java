package com.example.hexagonal.architecture.salud.application.port.in;

import java.util.UUID;

public record CancelAppointmentCommand(UUID appointmentId, String reasonDescription) {

    public CancelAppointmentCommand {
        if (appointmentId == null) {
            throw new IllegalArgumentException("appointmentId is required");
        }
        if (reasonDescription == null || reasonDescription.isBlank()) {
            throw new IllegalArgumentException("reasonDescription is required");
        }
    }
}
