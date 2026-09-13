package com.example.hexagonal.architecture.salud.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.hexagonal.architecture.salud.application.port.in.CancelAppointmentCommand;
import com.example.hexagonal.architecture.salud.application.port.in.CancelAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentNotFoundException;
import com.example.hexagonal.architecture.salud.domain.appointment.CancellationReason;

import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CancelAppointmentService implements CancelAppointmentUseCase {

    private final LoadAppointmentPort loadAppointmentPort;
    private final SaveAppointmentPort saveAppointmentPort;

    @Override
    public Mono<Appointment> cancel(CancelAppointmentCommand command) {
        return loadAppointmentPort.loadById(command.appointmentId())
                .switchIfEmpty(Mono.error(new AppointmentNotFoundException(command.appointmentId())))
                .map(appointment -> appointment.cancel(CancellationReason.now(command.reasonDescription())))
                .flatMap(saveAppointmentPort::save);
    }
}
