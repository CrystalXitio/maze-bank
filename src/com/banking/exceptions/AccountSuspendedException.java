package com.banking.exceptions;

public class AccountSuspendedException extends Exception {
    public AccountSuspendedException(String message) {
        super(message);
    }
}
