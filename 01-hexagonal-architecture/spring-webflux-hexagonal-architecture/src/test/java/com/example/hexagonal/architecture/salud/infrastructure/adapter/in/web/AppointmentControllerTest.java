package com.example.hexagonal.architecture.salud.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.salud.application.port.in.CancelAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.CompleteAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.ConfirmAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.FindAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.MarkAppointmentNoShowUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.RequestAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.RescheduleAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.StartAppointmentUseCase;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentStatus;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RequestAppointmentUseCase requestAppointmentUseCase;
    @MockitoBean
    private ConfirmAppointmentUseCase confirmAppointmentUseCase;
    @MockitoBean
    private StartAppointmentUseCase startAppointmentUseCase;
    @MockitoBean
    private CompleteAppointmentUseCase completeAppointmentUseCase;
    @MockitoBean
    private CancelAppointmentUseCase cancelAppointmentUseCase;
    @MockitoBean
    private RescheduleAppointmentUseCase rescheduleAppointmentUseCase;
    @MockitoBean
    private MarkAppointmentNoShowUseCase markAppointmentNoShowUseCase;
    @MockitoBean
    private FindAppointmentUseCase findAppointmentUseCase;

    @Test
    void findByIdReturns404WhenUseCaseReturnsEmpty() {
        UUID id = UUID.randomUUID();
        when(findAppointmentUseCase.findById(id)).thenReturn(Mono.empty());

        webTestClient.get().uri("/appointments/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void findByIdReturnsAppointmentWhenPresent() {
        UUID id = UUID.randomUUID();
        Appointment appointment = new Appointment(
                id,
                PatientId.newId(),
                PractitionerId.newId(),
                new TimeSlot(LocalDateTime.of(2026, 10, 1, 10, 0), LocalDateTime.of(2026, 10, 1, 10, 30)),
                "Chequeo",
                AppointmentStatus.REQUESTED,
                null);
        when(findAppointmentUseCase.findById(id)).thenReturn(Mono.just(appointment));

        webTestClient.get().uri("/appointments/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("REQUESTED");
    }

    @Test
    void requestWithMissingPatientIdIsRejectedWithBadRequest() {
        webTestClient.post().uri("/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"practitionerId":"%s","start":"2026-10-01T10:00:00","end":"2026-10-01T10:30:00","reason":"x"}
                        """.formatted(UUID.randomUUID()))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
