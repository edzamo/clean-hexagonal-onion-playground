package com.example.hexagonal.architecture.salud.domain.appointment;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentTest {

    private Appointment newRequestedAppointment() {
        TimeSlot schedule = new TimeSlot(
                LocalDateTime.of(2026, 10, 1, 10, 0),
                LocalDateTime.of(2026, 10, 1, 10, 30));
        return new Appointment(PatientId.newId(), PractitionerId.newId(), schedule, "Chequeo general");
    }

    @Test
    void newAppointmentStartsAsRequested() {
        Appointment appointment = newRequestedAppointment();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.REQUESTED);
    }

    @Test
    void confirmMovesFromRequestedToConfirmed() {
        Appointment appointment = newRequestedAppointment();

        appointment.confirm();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void confirmFailsWhenNotRequested() {
        Appointment appointment = newRequestedAppointment();
        appointment.confirm();

        assertThatThrownBy(appointment::confirm)
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test
    void fullHappyPathLifecycle() {
        Appointment appointment = newRequestedAppointment();

        appointment.confirm();
        appointment.start();
        appointment.complete();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    void startFailsWhenNotConfirmed() {
        Appointment appointment = newRequestedAppointment();

        assertThatThrownBy(appointment::start)
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test
    void cancelAllowedWhileRequestedOrConfirmed() {
        Appointment appointment = newRequestedAppointment();

        appointment.cancel(CancellationReason.now("El paciente no puede asistir"));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appointment.getCancellationReason().description())
                .isEqualTo("El paciente no puede asistir");
    }

    @Test
    void cancelFailsAfterCompleted() {
        Appointment appointment = newRequestedAppointment();
        appointment.confirm();
        appointment.start();
        appointment.complete();

        assertThatThrownBy(() -> appointment.cancel(CancellationReason.now("tarde")))
                .isInstanceOf(InvalidAppointmentTransitionException.class);
    }

    @Test
    void markNoShowOnlyFromConfirmed() {
        Appointment appointment = newRequestedAppointment();
        appointment.confirm();

        appointment.markNoShow();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
    }

    @Test
    void rescheduleChangesScheduleWhileNotStarted() {
        Appointment appointment = newRequestedAppointment();
        TimeSlot newSchedule = new TimeSlot(
                LocalDateTime.of(2026, 10, 2, 9, 0),
                LocalDateTime.of(2026, 10, 2, 9, 30));

        appointment.reschedule(newSchedule);

        assertThat(appointment.getSchedule()).isEqualTo(newSchedule);
    }
}
