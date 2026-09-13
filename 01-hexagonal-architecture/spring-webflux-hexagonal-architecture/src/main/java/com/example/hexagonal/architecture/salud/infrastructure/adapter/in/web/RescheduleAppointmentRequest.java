package com.example.hexagonal.architecture.salud.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RescheduleAppointmentRequest(@NotNull LocalDateTime start, @NotNull LocalDateTime end) {

    public TimeSlot toTimeSlot() {
        return new TimeSlot(start, end);
    }
}
