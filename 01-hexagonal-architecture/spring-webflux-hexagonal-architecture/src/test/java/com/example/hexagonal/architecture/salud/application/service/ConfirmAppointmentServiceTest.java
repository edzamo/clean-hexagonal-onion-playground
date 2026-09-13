package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentNotFoundException;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentStatus;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmAppointmentServiceTest {

    @Mock
    private LoadAppointmentPort loadAppointmentPort;

    @Mock
    private SaveAppointmentPort saveAppointmentPort;

    private Appointment requestedAppointment(UUID id) {
        TimeSlot schedule = new TimeSlot(LocalDateTime.of(2026, 10, 1, 10, 0), LocalDateTime.of(2026, 10, 1, 10, 30));
        return new Appointment(id, PatientId.newId(), PractitionerId.newId(), schedule, "Chequeo", AppointmentStatus.REQUESTED, null);
    }

    @Test
    void confirmsAnExistingRequestedAppointment() {
        UUID id = UUID.randomUUID();
        Appointment existing = requestedAppointment(id);
        when(loadAppointmentPort.loadById(id)).thenReturn(Mono.just(existing));
        when(saveAppointmentPort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        ConfirmAppointmentService service = new ConfirmAppointmentService(loadAppointmentPort, saveAppointmentPort);

        StepVerifier.create(service.confirm(id))
                .expectNextMatches(appointment -> appointment.getStatus() == AppointmentStatus.CONFIRMED)
                .verifyComplete();
    }

    @Test
    void failsWithNotFoundWhenAppointmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(loadAppointmentPort.loadById(id)).thenReturn(Mono.empty());

        ConfirmAppointmentService service = new ConfirmAppointmentService(loadAppointmentPort, saveAppointmentPort);

        StepVerifier.create(service.confirm(id))
                .expectError(AppointmentNotFoundException.class)
                .verify();
    }
}
