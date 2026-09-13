package com.example.cleanarchitecture.bank.usecases.port.in;

/**
 * Output Boundary — implemented by a Presenter in the interface-adapters
 * layer. The interactor never knows whether the result becomes JSON, a CLI
 * table, or anything else; it only knows it must "present" a Response Model.
 */
public interface GetAccountBalanceOutputBoundary {

    void present(AccountBalanceResponseModel responseModel);
}
