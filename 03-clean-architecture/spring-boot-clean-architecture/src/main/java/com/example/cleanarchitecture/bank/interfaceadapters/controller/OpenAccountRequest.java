package com.example.cleanarchitecture.bank.interfaceadapters.controller;

import com.example.cleanarchitecture.bank.usecases.port.in.OpenAccountCommand;
import jakarta.validation.constraints.NotBlank;

public record OpenAccountRequest(@NotBlank String holderName) {

    public OpenAccountCommand toCommand() {
        return new OpenAccountCommand(holderName);
    }
}
