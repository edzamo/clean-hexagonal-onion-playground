package com.example.cleanarchitecture.bank.entities;

public class Account {

    private AccountId id;
    private final String holderName;
    private Money balance;
    private AccountStatus status;

    public Account(String holderName) {
        if (holderName == null || holderName.isBlank()) {
            throw new IllegalArgumentException("holderName is required");
        }
        this.holderName = holderName;
        this.balance = Money.ZERO;
        this.status = AccountStatus.ACTIVE;
    }

    public Account(AccountId id, String holderName, Money balance, AccountStatus status) {
        this.id = id;
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
    }

    public AccountId getId() {
        return id;
    }

    public String getHolderName() {
        return holderName;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void deposit(Money amount) {
        requireActive();
        this.balance = this.balance.add(amount);
    }

    public void withdraw(Money amount) {
        requireActive();
        if (balance.isLessThan(amount)) {
            throw new InsufficientFundsException(
                    "Insufficient funds for a withdrawal of " + amount.amount());
        }
        this.balance = this.balance.subtract(amount);
    }

    public void close() {
        requireActive();
        if (balance.isGreaterThan(Money.ZERO)) {
            throw new InvalidAccountOperationException("Account cannot be closed with a positive balance");
        }
        this.status = AccountStatus.CLOSED;
    }

    private void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new InvalidAccountOperationException("Account is not active");
        }
    }
}
