package com.example.cleanarchitecture.bank.usecases.interactor;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.AccountNotFoundException;
import com.example.cleanarchitecture.bank.entities.AccountStatus;
import com.example.cleanarchitecture.bank.entities.InsufficientFundsException;
import com.example.cleanarchitecture.bank.entities.Money;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferCommand;
import com.example.cleanarchitecture.bank.usecases.port.in.TransferResult;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import com.example.cleanarchitecture.bank.usecases.port.out.TransactionGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferInteractorTest {

    @Mock
    private AccountGateway accountGateway;

    @Mock
    private TransactionGateway transactionGateway;

    private Account account(AccountId id, String balance) {
        return new Account(id, "holder", Money.of(balance), AccountStatus.ACTIVE);
    }

    @Test
    void transfersBetweenTwoDifferentAccounts() {
        AccountId sourceId = AccountId.newId();
        AccountId targetId = AccountId.newId();
        when(accountGateway.findById(sourceId)).thenReturn(Optional.of(account(sourceId, "100.00")));
        when(accountGateway.findById(targetId)).thenReturn(Optional.of(account(targetId, "20.00")));
        when(accountGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransferInteractor interactor = new TransferInteractor(accountGateway, transactionGateway);

        TransferResult result = interactor.transfer(new TransferCommand(sourceId, targetId, Money.of("40.00")));

        assertThat(result.sourceAccount().getBalance()).isEqualTo(Money.of("60.00"));
        assertThat(result.targetAccount().getBalance()).isEqualTo(Money.of("60.00"));
    }

    @Test
    void failsWhenSourceAccountDoesNotExist() {
        AccountId sourceId = AccountId.newId();
        AccountId targetId = AccountId.newId();
        when(accountGateway.findById(sourceId)).thenReturn(Optional.empty());

        TransferInteractor interactor = new TransferInteractor(accountGateway, transactionGateway);

        assertThatThrownBy(() -> interactor.transfer(new TransferCommand(sourceId, targetId, Money.of("10.00"))))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void failsWhenSourceHasInsufficientFunds() {
        AccountId sourceId = AccountId.newId();
        AccountId targetId = AccountId.newId();
        when(accountGateway.findById(sourceId)).thenReturn(Optional.of(account(sourceId, "10.00")));
        when(accountGateway.findById(targetId)).thenReturn(Optional.of(account(targetId, "0.00")));

        TransferInteractor interactor = new TransferInteractor(accountGateway, transactionGateway);

        assertThatThrownBy(() -> interactor.transfer(new TransferCommand(sourceId, targetId, Money.of("50.00"))))
                .isInstanceOf(InsufficientFundsException.class);
    }
}
