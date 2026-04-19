package com.banking.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class DatabaseManager {

    private static final String DB_NAME       = "bank.db";
    private static final String URL           = "jdbc:sqlite:" + DB_NAME;
    /** Path to the pre-packaged demonstration database resource. */
    private static final String BUNDLED_RESOURCE = "/com/banking/resources/bank.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        File dbFile = new File(DB_NAME);

        /* Phase 1: Attempt to extract the embedded demonstration database if no local instance exists. */
        if (!dbFile.exists()) {
            if (extractBundledDatabase(dbFile)) {
                System.out.println("[DB] Extracted bundled demo database → " + DB_NAME);
                return; /* Extraction successful; database initialization complete. */
            }
        }

        /* Phase 2: Fallback initialization. 
         * Executes if the local database already exists or if the resource extraction fails.
         */
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

            /* Initialize root administrator credential wrapper. */
            String injectAdmin = "INSERT OR IGNORE INTO users "
                    + "(customer_id, full_name, password_hash, email, phone_number, is_admin, is_suspended) "
                    + "VALUES (?, ?, ?, '', '', 1, 0)";
            try (PreparedStatement adminStmt = conn.prepareStatement(injectAdmin)) {
                adminStmt.setString(1, "admin");
                adminStmt.setString(2, "Master Administrator");
                adminStmt.setString(3, SecurityUtils.hashPassword("admin123"));
                adminStmt.executeUpdate();
            }

            /* Seed generic demonstration accounts to bypass missing resource payloads. */
            String injectUser = "INSERT OR IGNORE INTO users "
                    + "(customer_id, full_name, password_hash, email, phone_number, is_admin, is_suspended) "
                    + "VALUES (?, ?, ?, '', '', 0, 0)";
            String injectAcc  = "INSERT OR IGNORE INTO accounts "
                    + "(account_id, customer_id, balance, account_type) VALUES (?, ?, 0.0, ?)";

            try (PreparedStatement usrStmt = conn.prepareStatement(injectUser);
                 PreparedStatement accStmt = conn.prepareStatement(injectAcc)) {

                /* Primary demonstration identity 1 */
                usrStmt.setString(1, "07168386");
                usrStmt.setString(2, "User1");
                usrStmt.setString(3, SecurityUtils.hashPassword("1234567k"));
                usrStmt.executeUpdate();
                accStmt.setString(1, "CHK-76D3C"); accStmt.setString(2, "07168386"); accStmt.setString(3, "Checking"); accStmt.executeUpdate();
                accStmt.setString(1, "SAV-9286E"); accStmt.setString(2, "07168386"); accStmt.setString(3, "Savings");  accStmt.executeUpdate();

                /* Primary demonstration identity 2 */
                usrStmt.setString(1, "35395655");
                usrStmt.setString(2, "User2");
                usrStmt.setString(3, SecurityUtils.hashPassword("1234567a"));
                usrStmt.executeUpdate();
                accStmt.setString(1, "CHK-478F4"); accStmt.setString(2, "35395655"); accStmt.setString(3, "Checking"); accStmt.executeUpdate();
                accStmt.setString(1, "SAV-593DA"); accStmt.setString(2, "35395655"); accStmt.setString(3, "Savings");  accStmt.executeUpdate();

                /* Primary demonstration identity 3 */
                usrStmt.setString(1, "69864325");
                usrStmt.setString(2, "User3");
                usrStmt.setString(3, SecurityUtils.hashPassword("1234567p"));
                usrStmt.executeUpdate();
                accStmt.setString(1, "CHK-5954E"); accStmt.setString(2, "69864325"); accStmt.setString(3, "Checking"); accStmt.executeUpdate();
                accStmt.setString(1, "SAV-6B268"); accStmt.setString(2, "69864325"); accStmt.setString(3, "Savings");  accStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts the embedded demonstration database from the application archive to the local directory.
     *
     * @param target The target file definition for extraction.
     * @return true if the resource stream was successfully copied, false otherwise.
     */
    private static boolean extractBundledDatabase(File target) {
        try (InputStream in = DatabaseManager.class.getResourceAsStream(BUNDLED_RESOURCE)) {
            if (in == null) return false;
            Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
