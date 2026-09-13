package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.in.FindAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindAppointmentService implements FindAppointmentUseCase {

    private final LoadAppointmentPort loadAppointmentPort;

    @Override
    public Mono<Appointment> findById(UUID appointmentId) {
        return loadAppointmentPort.loadById(appointmentId);
    }
}
