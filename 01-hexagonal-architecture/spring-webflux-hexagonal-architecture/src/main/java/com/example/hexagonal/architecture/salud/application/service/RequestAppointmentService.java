package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.in.RequestAppointmentCommand;
import com.example.hexagonal.architecture.salud.application.port.in.RequestAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class RequestAppointmentService implements RequestAppointmentUseCase {

    private final SaveAppointmentPort saveAppointmentPort;

    @Override
    public Mono<Appointment> request(RequestAppointmentCommand command) {
        return Mono.defer(() -> saveAppointmentPort.save(
                new Appointment(command.patientId(), command.practitionerId(), command.schedule(), command.reason())));
    }
}
