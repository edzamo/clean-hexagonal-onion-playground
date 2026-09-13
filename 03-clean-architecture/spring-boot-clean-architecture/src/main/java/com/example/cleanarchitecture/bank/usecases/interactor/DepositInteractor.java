package com.example.cleanarchitecture.bank.usecases.interactor;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountNotFoundException;
import com.example.cleanarchitecture.bank.entities.Transaction;
import com.example.cleanarchitecture.bank.entities.TransactionType;
import com.example.cleanarchitecture.bank.usecases.port.in.DepositCommand;
import com.example.cleanarchitecture.bank.usecases.port.in.DepositUseCase;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import com.example.cleanarchitecture.bank.usecases.port.out.TransactionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DepositInteractor implements DepositUseCase {

    private final AccountGateway accountGateway;
    private final TransactionGateway transactionGateway;

    @Override
    @Transactional
    public Account deposit(DepositCommand command) {
        Account account = accountGateway.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.deposit(command.amount());
        Account saved = accountGateway.save(account);
        transactionGateway.save(Transaction.occur(
                command.accountId(), TransactionType.DEPOSIT, command.amount(), null));
        return saved;
    }
}
