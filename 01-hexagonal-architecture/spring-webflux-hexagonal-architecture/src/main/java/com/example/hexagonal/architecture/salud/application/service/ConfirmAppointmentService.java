package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.in.ConfirmAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ConfirmAppointmentService implements ConfirmAppointmentUseCase {

    private final LoadAppointmentPort loadAppointmentPort;
    private final SaveAppointmentPort saveAppointmentPort;

    @Override
    public Mono<Appointment> confirm(UUID appointmentId) {
        return loadAppointmentPort.loadById(appointmentId)
                .switchIfEmpty(Mono.error(new AppointmentNotFoundException(appointmentId)))
                .map(Appointment::confirm)
                .flatMap(saveAppointmentPort::save);
    }
}
