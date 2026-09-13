package com.example.hexagonal.architecture.salud.application.port.out;

public class AppointmentPersistenceException extends RuntimeException {

    public AppointmentPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
