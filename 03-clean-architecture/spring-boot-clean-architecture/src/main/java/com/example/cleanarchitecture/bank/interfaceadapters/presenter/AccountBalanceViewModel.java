package com.example.cleanarchitecture.bank.interfaceadapters.presenter;

import java.util.UUID;

/**
 * View Model — presentation-ready shape, built by the Presenter from the
 * Response Model. This is what the Controller actually serializes to JSON.
 */
public record AccountBalanceViewModel(UUID accountId, String holderName, String formattedBalance, String status) {
}
