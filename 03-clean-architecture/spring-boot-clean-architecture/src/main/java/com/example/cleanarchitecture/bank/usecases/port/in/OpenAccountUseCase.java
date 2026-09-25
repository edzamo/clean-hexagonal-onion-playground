package com.example.cleanarchitecture.bank.usecases.port.in;

import com.example.cleanarchitecture.bank.entities.Account;

public interface OpenAccountUseCase {

    Account open(OpenAccountCommand command);
}
