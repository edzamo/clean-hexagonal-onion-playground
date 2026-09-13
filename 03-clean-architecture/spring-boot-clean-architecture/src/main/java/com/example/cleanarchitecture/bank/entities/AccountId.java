package com.example.cleanarchitecture.bank.entities;

import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        if (value == null) {
            throw new IllegalArgumentException("AccountId cannot be null");
        }
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }
}
