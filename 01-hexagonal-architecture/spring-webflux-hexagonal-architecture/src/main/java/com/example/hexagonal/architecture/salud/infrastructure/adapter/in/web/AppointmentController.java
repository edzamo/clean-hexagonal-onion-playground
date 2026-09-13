package com.example.hexagonal.architecture.salud.infrastructure.adapter.in.web;

import com.example.hexagonal.architecture.salud.application.port.in.CancelAppointmentCommand;
import com.example.hexagonal.architecture.salud.application.port.in.CancelAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.CompleteAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.ConfirmAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.FindAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.MarkAppointmentNoShowUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.RequestAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.RescheduleAppointmentCommand;
import com.example.hexagonal.architecture.salud.application.port.in.RescheduleAppointmentUseCase;
import com.example.hexagonal.architecture.salud.application.port.in.StartAppointmentUseCase;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final RequestAppointmentUseCase requestAppointmentUseCase;
    private final ConfirmAppointmentUseCase confirmAppointmentUseCase;
    private final StartAppointmentUseCase startAppointmentUseCase;
    private final CompleteAppointmentUseCase completeAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final RescheduleAppointmentUseCase rescheduleAppointmentUseCase;
    private final MarkAppointmentNoShowUseCase markAppointmentNoShowUseCase;
    private final FindAppointmentUseCase findAppointmentUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AppointmentResponse> request(@Valid @RequestBody RequestAppointmentRequest request) {
        return requestAppointmentUseCase.request(request.toCommand())
                .map(AppointmentResponse::from);
    }

    @GetMapping("/{id}")
    public Mono<AppointmentResponse> findById(@PathVariable UUID id) {
        return findAppointmentUseCase.findById(id)
                .switchIfEmpty(Mono.error(new AppointmentNotFoundException(id)))
                .map(AppointmentResponse::from);
    }

    @PostMapping("/{id}/confirm")
    public Mono<AppointmentResponse> confirm(@PathVariable UUID id) {
        return confirmAppointmentUseCase.confirm(id)
                .map(AppointmentResponse::from);
    }

    @PostMapping("/{id}/start")
    public Mono<AppointmentResponse> start(@PathVariable UUID id) {
        return startAppointmentUseCase.start(id)
                .map(AppointmentResponse::from);
    }

    @PostMapping("/{id}/complete")
    public Mono<AppointmentResponse> complete(@PathVariable UUID id) {
        return completeAppointmentUseCase.complete(id)
                .map(AppointmentResponse::from);
    }

    @PostMapping("/{id}/cancel")
    public Mono<AppointmentResponse> cancel(@PathVariable UUID id, @Valid @RequestBody CancelAppointmentRequest request) {
        return cancelAppointmentUseCase.cancel(new CancelAppointmentCommand(id, request.reasonDescription()))
                .map(AppointmentResponse::from);
    }

    @PostMapping("/{id}/reschedule")
    public Mono<AppointmentResponse> reschedule(@PathVariable UUID id, @Valid @RequestBody RescheduleAppointmentRequest request) {
        return rescheduleAppointmentUseCase.reschedule(new RescheduleAppointmentCommand(id, request.toTimeSlot()))
                .map(AppointmentResponse::from);
    }

    @PostMapping("/{id}/no-show")
    public Mono<AppointmentResponse> markNoShow(@PathVariable UUID id) {
        return markAppointmentNoShowUseCase.markNoShow(id)
                .map(AppointmentResponse::from);
    }
}
