package com.example.hexagonal.architecture.salud.application.port.out;

import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import reactor.core.publisher.Mono;

public interface SaveAppointmentPort {

    Mono<Appointment> save(Appointment appointment);
}
