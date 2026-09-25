package com.example.cleanarchitecture.bank.entities;

public class InvalidAccountOperationException extends RuntimeException {

    public InvalidAccountOperationException(String message) {
        super(message);
    }
}
