package com.example.cleanarchitecture.bank.usecases.port.in;

public record OpenAccountCommand(String holderName) {

    public OpenAccountCommand {
        if (holderName == null || holderName.isBlank()) {
            throw new IllegalArgumentException("holderName is required");
        }
    }
}
