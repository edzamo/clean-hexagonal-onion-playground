package com.example.cleanarchitecture.bank.usecases.interactor;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.usecases.port.in.OpenAccountCommand;
import com.example.cleanarchitecture.bank.usecases.port.in.OpenAccountUseCase;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OpenAccountInteractor implements OpenAccountUseCase {

    private final AccountGateway accountGateway;

    @Override
    public Account open(OpenAccountCommand command) {
        return accountGateway.save(new Account(command.holderName()));
    }
}
