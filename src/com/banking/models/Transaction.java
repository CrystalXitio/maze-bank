package com.banking.models;

import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 2L;
    
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER
    }
    
    private String transactionId;
    private double amount;
    private LocalDate timestamp;
    private String description;
    private TransactionType type;

    public Transaction(String transactionId, double amount, String description, TransactionType type) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.timestamp = LocalDate.now();
        this.description = description;
        this.type = type;
    }
    
    public Transaction(String transactionId, double amount, String description, TransactionType type, LocalDate timestamp) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
        this.type = type;
    }

    public String getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public LocalDate getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    public TransactionType getType() { return type; }
}
