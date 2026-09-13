package com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence;

import com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence.entity.AppointmentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SpringDataAppointmentRepository extends ReactiveCrudRepository<AppointmentEntity, UUID> {

    Flux<AppointmentEntity> findByPatientId(UUID patientId);

    Flux<AppointmentEntity> findByPractitionerId(UUID practitionerId);
}
