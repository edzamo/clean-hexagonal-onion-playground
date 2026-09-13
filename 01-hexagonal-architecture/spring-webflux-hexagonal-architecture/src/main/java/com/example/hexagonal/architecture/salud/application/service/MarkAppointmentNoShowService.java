package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.in.MarkAppointmentNoShowUseCase;
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
public class MarkAppointmentNoShowService implements MarkAppointmentNoShowUseCase {

    private final LoadAppointmentPort loadAppointmentPort;
    private final SaveAppointmentPort saveAppointmentPort;

    @Override
    public Mono<Appointment> markNoShow(UUID appointmentId) {
        return loadAppointmentPort.loadById(appointmentId)
                .switchIfEmpty(Mono.error(new AppointmentNotFoundException(appointmentId)))
                .map(Appointment::markNoShow)
                .flatMap(saveAppointmentPort::save);
    }
}
