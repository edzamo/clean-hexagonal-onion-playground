package com.example.cleanarchitecture.bank.usecases.interactor;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountNotFoundException;
import com.example.cleanarchitecture.bank.entities.Transaction;
import com.example.cleanarchitecture.bank.entities.TransactionType;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferCommand;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferResult;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferUseCase;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import com.example.cleanarchitecture.bank.usecases.port.out.TransactionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Unlike every use case in the salud/coffeeshop hexagonal playground (each of
 * which touched exactly one aggregate), a transfer necessarily spans two
 * independent Account aggregates. That coordination — "debit one, credit the
 * other, as a single atomic unit" — belongs here, in the interactor, not
 * inside Account itself: a single aggregate's methods can only enforce
 * invariants about its own state.
 */
@RequiredArgsConstructor
@Service
public class TransferInteractor implements TransferUseCase {

    private final AccountGateway accountGateway;
    private final TransactionGateway transactionGateway;

    @Override
    @Transactional
    public TransferResult transfer(TransferCommand command) {
        Account source = accountGateway.findById(command.sourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.sourceAccountId()));
        Account target = accountGateway.findById(command.targetAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.targetAccountId()));

        source.withdraw(command.amount());
        target.deposit(command.amount());

        Account savedSource = accountGateway.save(source);
        Account savedTarget = accountGateway.save(target);

        transactionGateway.save(Transaction.occur(
                command.sourceAccountId(), TransactionType.TRANSFER_OUT, command.amount(), command.targetAccountId()));
        transactionGateway.save(Transaction.occur(
                command.targetAccountId(), TransactionType.TRANSFER_IN, command.amount(), command.sourceAccountId()));

        return new TransferResult(savedSource, savedTarget);
    }
}
