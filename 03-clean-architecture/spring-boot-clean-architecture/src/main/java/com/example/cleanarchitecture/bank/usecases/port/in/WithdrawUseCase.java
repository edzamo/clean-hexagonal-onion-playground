package com.example.cleanarchitecture.bank.usecases.port.in;

import com.example.cleanarchitecture.bank.entities.Account;

public interface WithdrawUseCase {

    Account withdraw(WithdrawCommand command);
}
