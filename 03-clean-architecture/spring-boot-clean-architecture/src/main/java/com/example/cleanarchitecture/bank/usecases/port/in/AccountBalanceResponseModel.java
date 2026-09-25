package com.example.cleanarchitecture.bank.usecases.port.in;

import com.example.cleanarchitecture.bank.entities.AccountId;
import com.example.cleanarchitecture.bank.entities.AccountStatus;
import com.example.cleanarchitecture.bank.entities.Money;

/**
 * Response Model — plain data the interactor hands to the Output Boundary.
 * Deliberately not the domain {@code Account} itself: the use case layer
 * decides exactly what's relevant to report, independent of how it will be
 * presented.
 */
public record AccountBalanceResponseModel(
        AccountId accountId, String holderName, Money balance, AccountStatus status) {
}
