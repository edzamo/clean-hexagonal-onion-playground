package com.example.hexagonal.architecture.salud.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.salud.application.port.in.RequestAppointmentCommand;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestAppointmentRequest(@NotNull UUID patientId, @NotNull UUID practitionerId,
                                         @NotNull LocalDateTime start, @NotNull LocalDateTime end, String reason) {

    public RequestAppointmentCommand toCommand() {
        return new RequestAppointmentCommand(
                new PatientId(patientId),
                new PractitionerId(practitionerId),
                new TimeSlot(start, end),
                reason);
    }
}
