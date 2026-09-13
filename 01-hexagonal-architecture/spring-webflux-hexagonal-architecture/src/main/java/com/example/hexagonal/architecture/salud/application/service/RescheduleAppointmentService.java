package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.in.RescheduleAppointmentCommand;
import com.example.hexagonal.architecture.salud.application.port.in.RescheduleAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class RescheduleAppointmentService implements RescheduleAppointmentUseCase {

    private final LoadAppointmentPort loadAppointmentPort;
    private final SaveAppointmentPort saveAppointmentPort;

    @Override
    public Mono<Appointment> reschedule(RescheduleAppointmentCommand command) {
        return loadAppointmentPort.loadById(command.appointmentId())
                .switchIfEmpty(Mono.error(new AppointmentNotFoundException(command.appointmentId())))
                .map(appointment -> appointment.reschedule(command.newSchedule()))
                .flatMap(saveAppointmentPort::save);
    }
}
