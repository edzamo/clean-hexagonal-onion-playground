package com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence;

import com.example.hexagonal.architecture.salud.application.port.out.LoadAppointmentPort;
import com.example.hexagonal.architecture.salud.application.port.out.SaveAppointmentPort;
import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence.entity.AppointmentEntity;
import com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence.mapper.AppointmentPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class AppointmentPersistenceAdapter implements LoadAppointmentPort, SaveAppointmentPort {

    private final SpringDataAppointmentRepository repository;
    private final R2dbcEntityTemplate entityTemplate;
    private final AppointmentPersistenceMapper mapper;

    @Override
    public Mono<Appointment> loadById(UUID appointmentId) {
        return repository.findById(appointmentId).map(mapper::toDomain);
    }

    @Override
    public Flux<Appointment> loadByPatientId(PatientId patientId) {
        return repository.findByPatientId(patientId.value()).map(mapper::toDomain);
    }

    @Override
    public Flux<Appointment> loadByPractitionerId(PractitionerId practitionerId) {
        return repository.findByPractitionerId(practitionerId.value()).map(mapper::toDomain);
    }

    @Override
    public Mono<Appointment> save(Appointment appointment) {
        boolean isNew = appointment.getId() == null;
        AppointmentEntity entity = mapper.toEntity(appointment);
        Mono<AppointmentEntity> persisted = isNew
                ? entityTemplate.insert(entity)
                : entityTemplate.update(entity);
        return persisted.map(mapper::toDomain);
    }
}
