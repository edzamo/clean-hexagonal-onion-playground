package com.example.cleanarchitecture.bank.usecases.port.in;

public interface TransferUseCase {

    TransferResult transfer(TransferCommand command);
}
