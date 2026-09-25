package com.example.cleanarchitecture.bank.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void newAccountStartsActiveWithZeroBalance() {
        Account account = new Account("Ana");

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualTo(Money.ZERO);
    }

    @Test
    void depositIncreasesBalance() {
        Account account = new Account("Ana");

        account.deposit(Money.of("100.00"));

        assertThat(account.getBalance()).isEqualTo(Money.of("100.00"));
    }

    @Test
    void withdrawDecreasesBalance() {
        Account account = new Account("Ana");
        account.deposit(Money.of("100.00"));

        account.withdraw(Money.of("40.00"));

        assertThat(account.getBalance()).isEqualTo(Money.of("60.00"));
    }

    @Test
    void withdrawFailsWithInsufficientFunds() {
        Account account = new Account("Ana");
        account.deposit(Money.of("10.00"));

        assertThatThrownBy(() -> account.withdraw(Money.of("50.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void closeFailsWithPositiveBalance() {
        Account account = new Account("Ana");
        account.deposit(Money.of("10.00"));

        assertThatThrownBy(account::close)
                .isInstanceOf(InvalidAccountOperationException.class);
    }

    @Test
    void closeSucceedsWithZeroBalance() {
        Account account = new Account("Ana");

        account.close();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void depositFailsOnClosedAccount() {
        Account account = new Account("Ana");
        account.close();

        assertThatThrownBy(() -> account.deposit(Money.of("10.00")))
                .isInstanceOf(InvalidAccountOperationException.class);
    }
}
