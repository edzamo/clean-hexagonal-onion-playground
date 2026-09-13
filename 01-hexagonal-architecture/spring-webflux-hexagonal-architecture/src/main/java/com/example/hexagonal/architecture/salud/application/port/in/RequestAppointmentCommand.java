package com.example.hexagonal.architecture.salud.application.port.in;

import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;

public record RequestAppointmentCommand(PatientId patientId, PractitionerId practitionerId,
                                         TimeSlot schedule, String reason) {

    public RequestAppointmentCommand {
        if (patientId == null || practitionerId == null || schedule == null) {
            throw new IllegalArgumentException("patientId, practitionerId and schedule are required");
        }
    }
}
