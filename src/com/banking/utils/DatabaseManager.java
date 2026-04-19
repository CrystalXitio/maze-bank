package com.banking.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:bank.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                    + "customer_id TEXT PRIMARY KEY,"
                    + "full_name TEXT NOT NULL,"
                    + "password_hash TEXT NOT NULL,"
                    + "email TEXT DEFAULT '',"
                    + "phone_number TEXT DEFAULT '',"
                    + "is_admin INTEGER DEFAULT 0,"
                    + "is_suspended INTEGER DEFAULT 0"
                    + ");";
                    
            String createAccounts = "CREATE TABLE IF NOT EXISTS accounts ("
                    + "account_id TEXT PRIMARY KEY,"
                    + "customer_id TEXT NOT NULL,"
                    + "balance REAL NOT NULL,"
                    + "account_type TEXT NOT NULL,"
                    + "FOREIGN KEY(customer_id) REFERENCES users(customer_id)"
                    + ");";
                    
            String createTransactions = "CREATE TABLE IF NOT EXISTS transactions ("
                    + "transaction_id TEXT PRIMARY KEY,"
                    + "account_id TEXT NOT NULL,"
                    + "amount REAL NOT NULL,"
                    + "timestamp TEXT NOT NULL,"
                    + "description TEXT NOT NULL,"
                    + "type TEXT NOT NULL,"
                    + "FOREIGN KEY(account_id) REFERENCES accounts(account_id)"
                    + ");";

            stmt.execute(createUsers);
            stmt.execute(createAccounts);
            stmt.execute(createTransactions);
            
            String injectAdmin = "INSERT OR IGNORE INTO users (customer_id, full_name, password_hash, email, phone_number, is_admin, is_suspended) VALUES (?, ?, ?, '', '', 1, 0)";
            try (PreparedStatement adminStmt = conn.prepareStatement(injectAdmin)) {
                adminStmt.setString(1, "admin");
                adminStmt.setString(2, "Master Administrator");
                adminStmt.setString(3, SecurityUtils.hashPassword("admin123"));
                adminStmt.executeUpdate();
            }
            
            String injectSpecificUser = "INSERT OR IGNORE INTO users (customer_id, full_name, password_hash, email, phone_number, is_admin, is_suspended) VALUES (?, ?, ?, '', '', 0, 0)";
            String injectSpecificAccount = "INSERT OR IGNORE INTO accounts (account_id, customer_id, balance, account_type) VALUES (?, ?, 0.0, ?)";
            
            try (PreparedStatement usrStmt = conn.prepareStatement(injectSpecificUser);
                 PreparedStatement accStmt = conn.prepareStatement(injectSpecificAccount)) {
                 

                usrStmt.setString(1, "07168386");
                usrStmt.setString(2, "Krishna");
                usrStmt.setString(3, SecurityUtils.hashPassword("1234567k"));
                usrStmt.executeUpdate();
                accStmt.setString(1, "CHK-76D3C"); accStmt.setString(2, "07168386"); accStmt.setString(3, "Checking"); accStmt.executeUpdate();
                accStmt.setString(1, "SAV-9286E"); accStmt.setString(2, "07168386"); accStmt.setString(3, "Savings"); accStmt.executeUpdate();


                usrStmt.setString(1, "35395655");
                usrStmt.setString(2, "Agrim");
                usrStmt.setString(3, SecurityUtils.hashPassword("1234567a"));
                usrStmt.executeUpdate();
                accStmt.setString(1, "CHK-478F4"); accStmt.setString(2, "35395655"); accStmt.setString(3, "Checking"); accStmt.executeUpdate();
                accStmt.setString(1, "SAV-593DA"); accStmt.setString(2, "35395655"); accStmt.setString(3, "Savings"); accStmt.executeUpdate();
                

                usrStmt.setString(1, "69864325");
                usrStmt.setString(2, "Pranshu");
                usrStmt.setString(3, SecurityUtils.hashPassword("1234567p"));
                usrStmt.executeUpdate();
                accStmt.setString(1, "CHK-5954E"); accStmt.setString(2, "69864325"); accStmt.setString(3, "Checking"); accStmt.executeUpdate();
                accStmt.setString(1, "SAV-6B268"); accStmt.setString(2, "69864325"); accStmt.setString(3, "Savings"); accStmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
