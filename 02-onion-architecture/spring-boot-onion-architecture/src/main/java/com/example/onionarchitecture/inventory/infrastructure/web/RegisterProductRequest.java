package com.example.onionarchitecture.inventory.infrastructure.web;

import com.example.onionarchitecture.inventory.application.dto.RegisterProductCommand;
import jakarta.validation.constraints.NotBlank;

public record RegisterProductRequest(@NotBlank String sku, @NotBlank String name) {

    public RegisterProductCommand toCommand() {
        return new RegisterProductCommand(sku, name);
    }
}
