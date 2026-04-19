package com.banking.services;

import com.banking.exceptions.AccountSuspendedException;
import com.banking.models.Account;
import com.banking.models.Transaction;
import com.banking.models.Transaction.TransactionType;
import com.banking.models.User;
import com.banking.utils.DatabaseManager;
import com.banking.utils.SecurityUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankingService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public BankingService() {
        DatabaseManager.initializeDatabase();
    }
    
    public double getCurrentBalance(String accountId) {
        String query = "SELECT balance FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void updateAccountBalance(String accountId, double newBalance) throws SQLException {
        String query = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountId);
            pstmt.executeUpdate();
        }
    }

    public synchronized String registerUser(String fullName, String rawPassword) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String customerId = "";
            boolean unique = false;
            while(!unique) {
                customerId = String.format("%08d", new java.util.Random().nextInt(100000000));
                String checkQuery = "SELECT customer_id FROM users WHERE customer_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, customerId);
                    if (!checkStmt.executeQuery().next()) unique = true;
                }
            }
            
            String hash = SecurityUtils.hashPassword(rawPassword);
            String insertUser = "INSERT INTO users (customer_id, full_name, password_hash, email, phone_number, is_admin, is_suspended) VALUES (?, ?, ?, '', '', 0, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
                pstmt.setString(1, customerId);
                pstmt.setString(2, fullName);
                pstmt.setString(3, hash);
                pstmt.executeUpdate();
            }
            
            createAccount(conn, customerId, "CHK-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase(), "Checking");
            createAccount(conn, customerId, "SAV-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase(), "Savings");
            return customerId;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void createAccount(Connection conn, String customerId, String accountId, String type) throws SQLException {
        String insertAcc = "INSERT INTO accounts (account_id, customer_id, balance, account_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertAcc)) {
            pstmt.setString(1, accountId);
            pstmt.setString(2, customerId);
            pstmt.setDouble(3, 0.0);
            pstmt.setString(4, type);
            pstmt.executeUpdate();
        }
    }

    public User authenticate(String customerId, String rawPassword) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String query = "SELECT full_name, password_hash, email, phone_number, is_admin, is_suspended FROM users WHERE customer_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, customerId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String hash = SecurityUtils.hashPassword(rawPassword);
                    if (rs.getString("password_hash").equals(hash)) {
                        boolean isAdmin = rs.getInt("is_admin") == 1;
                        boolean isSuspended = rs.getInt("is_suspended") == 1;
                        User user = new User(customerId, rs.getString("full_name"), hash, rs.getString("email"), rs.getString("phone_number"), isAdmin, isSuspended);
                        loadUserAccounts(conn, user);
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void updateUserProfile(String customerId, String email, String phone) throws SQLException {
        String update = "UPDATE users SET email = ?, phone_number = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(update)) {
            pstmt.setString(1, email);
            pstmt.setString(2, phone);
            pstmt.setString(3, customerId);
            pstmt.executeUpdate();
        }
    }
    
    private void loadUserAccounts(Connection conn, User user) throws SQLException {
        String query = "SELECT account_id, balance, account_type FROM accounts WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, user.getCustomerId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Account acc = new Account(rs.getString("account_id"), rs.getString("account_type"), rs.getDouble("balance"));
                user.addAccount(acc);
            }
        }
    }

    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLetter = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }

    private void verifyAccountActive(Connection conn, String accountId) throws SQLException, AccountSuspendedException {
        String query = "SELECT u.is_suspended FROM accounts a JOIN users u ON a.customer_id = u.customer_id WHERE a.account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("is_suspended") == 1) {
                throw new AccountSuspendedException("Transaction blocked: Account " + accountId + " is currently suspended.");
            }
        }
    }

    public void transferFunds(String sourceAccountId, String destAccountId, double amount) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Transfer amount must be positive.");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                verifyAccountActive(conn, sourceAccountId);
                verifyAccountActive(conn, destAccountId);
                
                double sourceBalance = getCurrentBalanceWithConn(conn, sourceAccountId);
                if (sourceBalance < amount) throw new IllegalArgumentException("Insufficient funds.");
                
                double destBalance = getCurrentBalanceWithConn(conn, destAccountId);
                
                updateAccountBalanceWithConn(conn, sourceAccountId, sourceBalance - amount);
                updateAccountBalanceWithConn(conn, destAccountId, destBalance + amount);
                
                recordTransactionObj(conn, sourceAccountId, amount, "Transfer to " + destAccountId, TransactionType.TRANSFER, LocalDate.now());
                recordTransactionObj(conn, destAccountId, amount, "Transfer from " + sourceAccountId, TransactionType.TRANSFER, LocalDate.now());
                
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    private double getCurrentBalanceWithConn(Connection conn, String accountId) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
            throw new SQLException("Account not found");
        }
    }
    
    private void updateAccountBalanceWithConn(Connection conn, String accountId, double newBalance) throws SQLException {
        String query = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, accountId);
            pstmt.executeUpdate();
        }
    }

    public void withdraw(String accountId, double amount) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        try (Connection conn = DatabaseManager.getConnection()) {
            verifyAccountActive(conn, accountId);
            double currentBalance = getCurrentBalanceWithConn(conn, accountId);
            if (currentBalance < amount) throw new IllegalArgumentException("Insufficient funds.");
            
            updateAccountBalanceWithConn(conn, accountId, currentBalance - amount);
            recordTransactionObj(conn, accountId, amount, "Withdrawal", TransactionType.WITHDRAWAL, LocalDate.now());
        }
    }
    
    public void deposit(String accountId, double amount) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        try (Connection conn = DatabaseManager.getConnection()) {
            verifyAccountActive(conn, accountId);
            double currentBalance = getCurrentBalanceWithConn(conn, accountId);
            updateAccountBalanceWithConn(conn, accountId, currentBalance + amount);
            recordTransactionObj(conn, accountId, amount, "Deposit", TransactionType.DEPOSIT, LocalDate.now());
        }
    }

    private void recordTransactionObj(Connection conn, String accountId, double amount, String description, TransactionType type, LocalDate date) throws SQLException {
        String insertTx = "INSERT INTO transactions (transaction_id, account_id, amount, timestamp, description, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertTx)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, accountId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, date.format(FORMATTER));
            pstmt.setString(5, description);
            pstmt.setString(6, type.name());
            pstmt.executeUpdate();
        }
    }
    
    public void addTransactionDirect(String accountId, Transaction t) {
        try (Connection conn = DatabaseManager.getConnection()) {
            recordTransactionObj(conn, accountId, t.getAmount(), t.getDescription(), t.getType(), t.getTimestamp());
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Transaction> getTransactions(String accountId) {
        List<Transaction> txs = new ArrayList<>();
        String query = "SELECT transaction_id, amount, timestamp, description, type FROM transactions WHERE account_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("transaction_id");
                double amt = rs.getDouble("amount");
                String ts = rs.getString("timestamp");
                String desc = rs.getString("description");
                TransactionType type = TransactionType.valueOf(rs.getString("type"));
                
                LocalDate d = LocalDate.parse(ts, FORMATTER);
                txs.add(new Transaction(id, amt, desc, type, d));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txs;
    }

    public double calculateMonthlyTotal(String accountId, YearMonth month, TransactionType type) {
        List<Transaction> txs = getTransactions(accountId);
        return txs.stream()
                .filter(t -> YearMonth.from(t.getTimestamp()).equals(month))
                .filter(t -> t.getType() == type)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculatePercentageChange(double currentMonth, double lastMonth) {
        if (lastMonth == 0) return currentMonth > 0 ? 100.0 : 0.0;
        return ((currentMonth - lastMonth) / lastMonth) * 100.0;
    }
    
    public Account getAccountById(String targetId) {
        String query = "SELECT account_id, balance, account_type FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, targetId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Account(rs.getString("account_id"), rs.getString("account_type"), rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllSystemUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT customer_id, full_name, password_hash, email, phone_number, is_admin, is_suspended FROM users";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User u = new User(
                    rs.getString("customer_id"), 
                    rs.getString("full_name"), 
                    rs.getString("password_hash"), 
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getInt("is_admin") == 1, 
                    rs.getInt("is_suspended") == 1
                );
                loadUserAccounts(conn, u);
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<Object[]> getAllSystemTransactions() {
        List<Object[]> masterLedger = new ArrayList<>();
        String query = "SELECT t.timestamp, t.account_id, a.customer_id, t.amount, t.type, t.description " +
                       "FROM transactions t JOIN accounts a ON t.account_id = a.account_id";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                masterLedger.add(new Object[]{
                    rs.getString("timestamp"),
                    rs.getString("customer_id"),
                    rs.getString("account_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return masterLedger;
    }

    public void toggleUserSuspension(String customerId, boolean suspend) throws SQLException {
        String query = "UPDATE users SET is_suspended = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, suspend ? 1 : 0);
            pstmt.setString(2, customerId);
            pstmt.executeUpdate();
        }
    }

    public void adminUpdateUserExtendedInfo(String customerId, String fullName, String email, String phone) throws SQLException {
        String update = "UPDATE users SET full_name = ?, email = ?, phone_number = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(update)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, customerId);
            pstmt.executeUpdate();
        }
    }

    public void forceBalanceAdjustment(String accountId, double amount, String reason) throws SQLException {
        if (amount == 0) return;
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                double currentBalance = getCurrentBalanceWithConn(conn, accountId);
                double newBalance = currentBalance + amount;
                updateAccountBalanceWithConn(conn, accountId, newBalance);
                
                TransactionType type = amount > 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL;
                recordTransactionObj(conn, accountId, Math.abs(amount), "[ADMIN OVERRIDE] " + reason, type, LocalDate.now());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void deleteUser(String customerId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {

                String delTx = "DELETE FROM transactions WHERE account_id IN (SELECT account_id FROM accounts WHERE customer_id = ?)";
                try (PreparedStatement ps = conn.prepareStatement(delTx)) {
                    ps.setString(1, customerId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM accounts WHERE customer_id = ?")) {
                    ps.setString(1, customerId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE customer_id = ?")) {
                    ps.setString(1, customerId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
