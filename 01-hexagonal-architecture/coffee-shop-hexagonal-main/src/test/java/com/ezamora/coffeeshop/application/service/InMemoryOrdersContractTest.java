package com.ezamora.coffeeshop.application.service;

import com.ezamora.coffeeshop.application.out.Orders;
import com.ezamora.coffeeshop.contract.OrdersContract;

class InMemoryOrdersContractTest extends OrdersContract {

    private final InMemoryOrders fake = new InMemoryOrders();

    @Override
    protected Orders orders() {
        return fake;
    }
}
