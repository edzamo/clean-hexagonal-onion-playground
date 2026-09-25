package com.example.cleanarchitecture.bank.usecases.interactor;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.AccountNotFoundException;
import com.example.cleanarchitecture.bank.entities.AccountStatus;
import com.example.cleanarchitecture.bank.entities.Money;
import com.example.cleanarchitecture.bank.usecases.port.in.AccountBalanceResponseModel;
import com.example.cleanarchitecture.bank.usecases.port.in.GetAccountBalanceOutputBoundary;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountBalanceInteractorTest {

    @Mock
    private AccountGateway accountGateway;

    @Mock
    private GetAccountBalanceOutputBoundary outputBoundary;

    @Test
    void presentsTheResponseModelInsteadOfReturningIt() {
        AccountId id = AccountId.newId();
        when(accountGateway.findById(id))
                .thenReturn(Optional.of(new Account(id, "Ana", Money.of("100.00"), AccountStatus.ACTIVE)));

        GetAccountBalanceInteractor interactor = new GetAccountBalanceInteractor(accountGateway, outputBoundary);
        interactor.getBalance(id);

        ArgumentCaptor<AccountBalanceResponseModel> captor = ArgumentCaptor.forClass(AccountBalanceResponseModel.class);
        verify(outputBoundary).present(captor.capture());
        assertThat(captor.getValue().holderName()).isEqualTo("Ana");
        assertThat(captor.getValue().balance()).isEqualTo(Money.of("100.00"));
    }

    @Test
    void failsWithNotFoundWhenAccountDoesNotExist() {
        AccountId id = AccountId.newId();
        when(accountGateway.findById(id)).thenReturn(Optional.empty());

        GetAccountBalanceInteractor interactor = new GetAccountBalanceInteractor(accountGateway, outputBoundary);

        assertThatThrownBy(() -> interactor.getBalance(id))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
