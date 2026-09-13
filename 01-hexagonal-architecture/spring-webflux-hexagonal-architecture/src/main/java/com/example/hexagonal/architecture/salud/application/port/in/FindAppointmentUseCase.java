package com.example.hexagonal.architecture.salud.application.port.in;

import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FindAppointmentUseCase {

    Mono<Appointment> findById(UUID appointmentId);
}
