package com.example.hexagonal.architecture.salud.domain.appointment;

public class InvalidAppointmentTransitionException extends RuntimeException {

    public InvalidAppointmentTransitionException(String message) {
        super(message);
    }
}
