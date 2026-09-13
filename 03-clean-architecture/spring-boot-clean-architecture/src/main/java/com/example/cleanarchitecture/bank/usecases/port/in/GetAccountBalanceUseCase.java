package com.example.cleanarchitecture.bank.usecases.port.in;

import com.example.cleanarchitecture.bank.entities.AccountId;

/**
 * Input Boundary — note it returns {@code void}. The result travels out
 * through the {@link GetAccountBalanceOutputBoundary}, not as a return
 * value; that indirection is the textbook Clean Architecture shape.
 */
public interface GetAccountBalanceUseCase {

    void getBalance(AccountId accountId);
}
