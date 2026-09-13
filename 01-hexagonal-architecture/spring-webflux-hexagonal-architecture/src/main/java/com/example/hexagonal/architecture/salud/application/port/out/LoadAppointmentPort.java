package com.example.hexagonal.architecture.salud.application.port.out;

import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LoadAppointmentPort {

    Mono<Appointment> loadById(UUID appointmentId);

    Flux<Appointment> loadByPatientId(PatientId patientId);

    Flux<Appointment> loadByPractitionerId(PractitionerId practitionerId);
}
