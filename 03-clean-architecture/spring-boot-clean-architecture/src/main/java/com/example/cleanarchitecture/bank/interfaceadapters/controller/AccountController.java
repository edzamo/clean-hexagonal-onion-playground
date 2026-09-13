package com.example.cleanarchitecture.bank.interfaceadapters.controller;

import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.interfaceadapters.presenter.AccountBalancePresenter;
import com.example.cleanarchitecture.bank.interfaceadapters.presenter.AccountBalanceViewModel;
import com.example.cleanarchitecture.bank.usecases.port.in.DepositCommand;
import com.example.cleanarchitecture.bank.usecases.port.in.DepositUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.GetAccountBalanceUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.OpenAccountUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferUseCase;
import com.example.cleanarchitecture.bank.usecases.port.in.WithdrawCommand;
import com.example.cleanarchitecture.bank.usecases.port.in.WithdrawUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final OpenAccountUseCase openAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final AccountBalancePresenter accountBalancePresenter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse open(@Valid @RequestBody OpenAccountRequest request) {
        return AccountResponse.from(openAccountUseCase.open(request.toCommand()));
    }

    @GetMapping("/{id}/balance")
    public AccountBalanceViewModel getBalance(@PathVariable UUID id) {
        getAccountBalanceUseCase.getBalance(new AccountId(id));
        return accountBalancePresenter.viewModel();
    }

    @PostMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(depositUseCase.deposit(new DepositCommand(new AccountId(id), request.toMoney())));
    }

    @PostMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable UUID id, @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(withdrawUseCase.withdraw(new WithdrawCommand(new AccountId(id), request.toMoney())));
    }

    @PostMapping("/{id}/transfer")
    public TransferResponse transfer(@PathVariable UUID id, @Valid @RequestBody TransferRequest request) {
        return TransferResponse.from(transferUseCase.transfer(request.toCommand(id)));
    }
}
