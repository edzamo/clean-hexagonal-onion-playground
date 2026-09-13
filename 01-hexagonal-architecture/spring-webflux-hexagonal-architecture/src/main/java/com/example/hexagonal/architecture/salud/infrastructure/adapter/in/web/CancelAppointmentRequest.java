package com.example.hexagonal.architecture.salud.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record CancelAppointmentRequest(@NotBlank String reasonDescription) {
}
