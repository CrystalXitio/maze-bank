package com.banking.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an authenticated system identity.
 * Encapsulates core customer profiles, role-based privileges, operational status, 
 * and linked ownership of multiple {@link Account} entities.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 4L;
    
    private String customerId;
    private String fullName;
    private String passwordHash;
    private String email;
    private String phoneNumber;
    private boolean isAdmin;
    private boolean isSuspended;
    private List<Account> accounts;

    public User(String customerId, String fullName, String passwordHash, String email, String phoneNumber, boolean isAdmin, boolean isSuspended) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.email = email != null ? email : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.isAdmin = isAdmin;
        this.isSuspended = isSuspended;
        this.accounts = new ArrayList<>();
    }

    public String getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phone) { this.phoneNumber = phone; }
    
    public boolean isAdmin() { return isAdmin; }
    public boolean isSuspended() { return isSuspended; }
    
    public List<Account> getAccounts() { return accounts; }
    public void addAccount(Account account) { accounts.add(account); }
}
