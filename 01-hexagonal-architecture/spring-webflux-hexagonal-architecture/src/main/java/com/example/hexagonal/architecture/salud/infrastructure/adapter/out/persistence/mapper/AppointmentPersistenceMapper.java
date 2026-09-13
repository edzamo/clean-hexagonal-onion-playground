package com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence.mapper;

import com.example.hexagonal.architecture.salud.domain.appointment.Appointment;
import com.example.hexagonal.architecture.salud.domain.appointment.AppointmentStatus;
import com.example.hexagonal.architecture.salud.domain.appointment.CancellationReason;
import com.example.hexagonal.architecture.salud.domain.appointment.PatientId;
import com.example.hexagonal.architecture.salud.domain.appointment.PractitionerId;
import com.example.hexagonal.architecture.salud.domain.appointment.TimeSlot;
import com.example.hexagonal.architecture.salud.infrastructure.adapter.out.persistence.entity.AppointmentEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AppointmentPersistenceMapper {

    public AppointmentEntity toEntity(Appointment appointment) {
        UUID id = appointment.getId() != null ? appointment.getId() : UUID.randomUUID();
        CancellationReason cancellationReason = appointment.getCancellationReason();
        return new AppointmentEntity(
                id,
                appointment.getPatientId().value(),
                appointment.getPractitionerId().value(),
                appointment.getSchedule().start(),
                appointment.getSchedule().end(),
                appointment.getReason(),
                appointment.getStatus().name(),
                cancellationReason != null ? cancellationReason.description() : null,
                cancellationReason != null ? cancellationReason.cancelledAt() : null);
    }

    public Appointment toDomain(AppointmentEntity entity) {
        CancellationReason cancellationReason = entity.cancellationDescription() != null
                ? new CancellationReason(entity.cancellationDescription(), entity.cancelledAt())
                : null;
        return new Appointment(
                entity.id(),
                new PatientId(entity.patientId()),
                new PractitionerId(entity.practitionerId()),
                new TimeSlot(entity.startTime(), entity.endTime()),
                entity.reason(),
                AppointmentStatus.valueOf(entity.status()),
                cancellationReason);
    }
}
