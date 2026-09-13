package com.example.hexagonal.architecture.salud.application.port.in;

import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;

import java.util.UUID;

public record RescheduleAppointmentCommand(UUID appointmentId, TimeSlot newSchedule) {

    public RescheduleAppointmentCommand {
        if (appointmentId == null || newSchedule == null) {
            throw new IllegalArgumentException("appointmentId and newSchedule are required");
        }
    }
}
