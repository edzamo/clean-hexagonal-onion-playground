package com.ezamora.coffeeshop.application.service;

import com.ezamora.coffeeshop.application.out.Payments;
import com.ezamora.coffeeshop.contract.PaymentsContract;

class InMemoryPaymentsContractTest extends PaymentsContract {

    private final InMemoryPayments fake = new InMemoryPayments();

    @Override
    protected Payments payments() {
        return fake;
    }
}
