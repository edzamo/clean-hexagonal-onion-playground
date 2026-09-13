package com.example.hexagonal.architecture.salud.application.port.in;

import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import reactor.core.publisher.Mono;

public interface RescheduleAppointmentUseCase {

    Mono<Appointment> reschedule(RescheduleAppointmentCommand command);
}
