package com.banking.models;

import java.io.Serializable;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String accountId;
    private double balance;
    private String accountType;

    public Account(String accountId, String accountType, double initialBalance) {
        this.accountId = accountId;
        this.accountType = accountType;
        this.balance = initialBalance;
    }

    public String getAccountId() { return accountId; }
    public double getBalance() { return balance; }
    public String getAccountType() { return accountType; }

    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
        }
    }
    
    @Override
    public String toString() {
        return accountType + " - " + accountId;
    }
}
