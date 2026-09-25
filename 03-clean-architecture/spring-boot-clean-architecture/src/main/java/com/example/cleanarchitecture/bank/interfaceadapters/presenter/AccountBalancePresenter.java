package com.example.cleanarchitecture.bank.interfaceadapters.presenter;

import com.example.cleanarchitecture.bank.usecases.port.in.AccountBalanceResponseModel;
import com.example.cleanarchitecture.bank.usecases.port.in.GetAccountBalanceOutputBoundary;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped: the interactor (a singleton) is constructor-injected with
 * a scoped proxy of this bean, so each HTTP request gets its own instance
 * transparently — {@code present()} sets state, the Controller reads it back
 * via {@link #viewModel()} once the interactor call returns, within the same
 * request. Without {@code @RequestScope} a singleton Presenter would leak
 * state across concurrent requests.
 */
@RequestScope
@Component
public class AccountBalancePresenter implements GetAccountBalanceOutputBoundary {

    private AccountBalanceViewModel viewModel;

    @Override
    public void present(AccountBalanceResponseModel responseModel) {
        this.viewModel = new AccountBalanceViewModel(
                responseModel.accountId().value(),
                responseModel.holderName(),
                "$" + responseModel.balance().amount().toPlainString(),
                responseModel.status().name());
    }

    public AccountBalanceViewModel viewModel() {
        return viewModel;
    }
}
