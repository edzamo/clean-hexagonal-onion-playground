package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.in.RequestAppointmentCommand;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestAppointmentServiceTest {

    @Mock
    private SaveAppointmentPort saveAppointmentPort;

    @Test
    void requestsANewAppointmentAndSavesIt() {
        RequestAppointmentService service = new RequestAppointmentService(saveAppointmentPort);
        RequestAppointmentCommand command = new RequestAppointmentCommand(
                PatientId.newId(),
                PractitionerId.newId(),
                new TimeSlot(LocalDateTime.of(2026, 10, 1, 10, 0), LocalDateTime.of(2026, 10, 1, 10, 30)),
                "Chequeo general");
        when(saveAppointmentPort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.request(command))
                .expectNextMatches(appointment -> appointment.getPatientId().equals(command.patientId()))
                .verifyComplete();

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(saveAppointmentPort).save(captor.capture());
        assertThat(captor.getValue().getReason()).isEqualTo("Chequeo general");
    }
}
