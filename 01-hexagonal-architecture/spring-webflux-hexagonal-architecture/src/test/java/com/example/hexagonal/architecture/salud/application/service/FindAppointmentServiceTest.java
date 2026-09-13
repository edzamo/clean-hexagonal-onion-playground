package com.example.hexagonal.architecture.salud.application.service;

import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAppointmentServiceTest {

    @Mock
    private LoadAppointmentPort loadAppointmentPort;

    @Test
    void returnsEmptyWhenAppointmentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(loadAppointmentPort.loadById(id)).thenReturn(Mono.empty());

        FindAppointmentService service = new FindAppointmentService(loadAppointmentPort);

        // A query returning empty is a valid result at the application layer —
        // no error here; translating it to 404 is the web adapter's job.
        StepVerifier.create(service.findById(id))
                .verifyComplete();
    }
}
