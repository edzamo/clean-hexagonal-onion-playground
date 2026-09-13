package com.example.hexagonal.architecture.salud.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(UUID id, UUID patientId, UUID practitionerId, LocalDateTime start,
                                   LocalDateTime end, String reason, AppointmentStatus status,
                                   String cancellationReason) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId().value(),
                appointment.getPractitionerId().value(),
                appointment.getSchedule().start(),
                appointment.getSchedule().end(),
                appointment.getReason(),
                appointment.getStatus(),
                appointment.getCancellationReason() != null ? appointment.getCancellationReason().description() : null);
    }
}
