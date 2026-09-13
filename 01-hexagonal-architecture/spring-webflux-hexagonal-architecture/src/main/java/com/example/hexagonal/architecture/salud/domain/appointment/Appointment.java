package com.example.hexagonal.architecture.salud.domain.appointment;

import java.util.UUID;

public class Appointment {

    private UUID id;
    private final PatientId patientId;
    private final PractitionerId practitionerId;
    private TimeSlot schedule;
    private final String reason;
    private AppointmentStatus status = AppointmentStatus.REQUESTED;
    private CancellationReason cancellationReason;

    public Appointment(PatientId patientId, PractitionerId practitionerId, TimeSlot schedule, String reason) {
        this.patientId = patientId;
        this.practitionerId = practitionerId;
        this.schedule = schedule;
        this.reason = reason;
    }

    public Appointment(UUID id, PatientId patientId, PractitionerId practitionerId, TimeSlot schedule,
                        String reason, AppointmentStatus status, CancellationReason cancellationReason) {
        this.id = id;
        this.patientId = patientId;
        this.practitionerId = practitionerId;
        this.schedule = schedule;
        this.reason = reason;
        this.status = status;
        this.cancellationReason = cancellationReason;
    }

    public UUID getId() {
        return id;
    }

    public PatientId getPatientId() {
        return patientId;
    }

    public PractitionerId getPractitionerId() {
        return practitionerId;
    }

    public TimeSlot getSchedule() {
        return schedule;
    }

    public String getReason() {
        return reason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public boolean canBeCancelled() {
        return status == AppointmentStatus.REQUESTED || status == AppointmentStatus.CONFIRMED;
    }

    public Appointment confirm() {
        if (status != AppointmentStatus.REQUESTED) {
            throw new InvalidAppointmentTransitionException("Only a requested appointment can be confirmed");
        }
        status = AppointmentStatus.CONFIRMED;
        return this;
    }

    public Appointment reschedule(TimeSlot newSchedule) {
        if (status != AppointmentStatus.REQUESTED && status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentTransitionException("Appointment can no longer be rescheduled");
        }
        this.schedule = newSchedule;
        return this;
    }

    public Appointment start() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentTransitionException("Only a confirmed appointment can start");
        }
        status = AppointmentStatus.IN_PROGRESS;
        return this;
    }

    public Appointment complete() {
        if (status != AppointmentStatus.IN_PROGRESS) {
            throw new InvalidAppointmentTransitionException("Only an in-progress appointment can be completed");
        }
        status = AppointmentStatus.COMPLETED;
        return this;
    }

    public Appointment cancel(CancellationReason reason) {
        if (!canBeCancelled()) {
            throw new InvalidAppointmentTransitionException("Appointment can no longer be cancelled");
        }
        status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
        return this;
    }

    public Appointment markNoShow() {
        if (status != AppointmentStatus.CONFIRMED) {
            throw new InvalidAppointmentTransitionException("Only a confirmed appointment can be marked as no-show");
        }
        status = AppointmentStatus.NO_SHOW;
        return this;
    }
}
