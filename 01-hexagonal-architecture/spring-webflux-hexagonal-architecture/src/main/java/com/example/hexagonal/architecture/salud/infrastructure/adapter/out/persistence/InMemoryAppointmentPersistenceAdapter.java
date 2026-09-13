package com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence;

import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAppointmentPersistenceAdapter implements LoadAppointmentPort, SaveAppointmentPort {

    private final Map<UUID, Appointment> store = new ConcurrentHashMap<>();

    @Override
    public Mono<Appointment> loadById(UUID appointmentId) {
        return Mono.justOrEmpty(store.get(appointmentId));
    }

    @Override
    public Flux<Appointment> loadByPatientId(PatientId patientId) {
        return Flux.fromIterable(store.values())
                .filter(appointment -> appointment.getPatientId().equals(patientId));
    }

    @Override
    public Flux<Appointment> loadByPractitionerId(PractitionerId practitionerId) {
        return Flux.fromIterable(store.values())
                .filter(appointment -> appointment.getPractitionerId().equals(practitionerId));
    }

    @Override
    public Mono<Appointment> save(Appointment appointment) {
        return Mono.fromCallable(() -> {
            UUID id = appointment.getId() != null ? appointment.getId() : UUID.randomUUID();
            Appointment toStore = new Appointment(
                    id,
                    appointment.getPatientId(),
                    appointment.getPractitionerId(),
                    appointment.getSchedule(),
                    appointment.getReason(),
                    appointment.getStatus(),
                    appointment.getCancellationReason());
            store.put(id, toStore);
            return toStore;
        });
    }
}
