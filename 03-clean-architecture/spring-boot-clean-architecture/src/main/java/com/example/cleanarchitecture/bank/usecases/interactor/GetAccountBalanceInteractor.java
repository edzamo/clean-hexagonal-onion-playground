package com.example.cleanarchitecture.bank.usecases.interactor;

import com.example.cleanarchitecture.bank.entities.Account;
import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.AccountNotFoundException;
import com.example.cleanarchitecture.bank.usecases.port.in.AccountBalanceResponseModel;
import com.example.cleanarchitecture.bank.usecases.port.in.GetAccountBalanceOutputBoundary;
import com.example.cleanarchitecture.bank.usecases.port.in.GetAccountBalanceUseCase;
import com.example.cleanarchitecture.bank.usecases.port.out.AccountGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GetAccountBalanceInteractor implements GetAccountBalanceUseCase {

    private final AccountGateway accountGateway;
    private final GetAccountBalanceOutputBoundary outputBoundary;

    @Override
    public void getBalance(AccountId accountId) {
        Account account = accountGateway.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        outputBoundary.present(new AccountBalanceResponseModel(
                account.getId(), account.getHolderName(), account.getBalance(), account.getStatus()));
    }
}
