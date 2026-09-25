package com.example.cleanarchitecture.bank.usecases.interactor;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountNotFoundException;
import com.example.cleanarchitecture.bank.entities.Transaction;
import com.example.cleanarchitecture.bank.entities.TransactionType;
import com.example.cleanarchitecture.bank.usecases.port.in.WithdrawCommand;
import com.example.cleanarchitecture.bank.usecases.port.in.WithdrawUseCase;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import com.example.cleanarchitecture.bank.usecases.port.out.TransactionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WithdrawInteractor implements WithdrawUseCase {

    private final AccountGateway accountGateway;
    private final TransactionGateway transactionGateway;

    @Override
    @Transactional
    public Account withdraw(WithdrawCommand command) {
        Account account = accountGateway.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        account.withdraw(command.amount());
        Account saved = accountGateway.save(account);
        transactionGateway.save(Transaction.occur(
                command.accountId(), TransactionType.WITHDRAWAL, command.amount(), null));
        return saved;
    }
}
