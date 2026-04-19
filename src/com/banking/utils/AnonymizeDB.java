package com.banking.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility class for database anonymization and preparation.
 * <p>
 * This tool duplicates the active SQLite database to the project's resources directory
 * and scrubs sensitive personal identifiable information (PII) to ensure safely redistributable 
 * demo data. It should be executed prior to packaging the application for distribution.
 */
public class AnonymizeDB {

    private static final String SOURCE_DB   = "bank.db";
    private static final String DEST_DIR    = "src/com/banking/resources";
    private static final String DEST_DB     = DEST_DIR + "/bank.db";

    public static void main(String[] args) throws Exception {

        File src = new File(SOURCE_DB);
        if (!src.exists()) {
            System.err.println("[ERROR] bank.db not found in project root. Run the app first to generate it.");
            System.exit(1);
        }

        /* Ensure the destination directory path exists. */
        new File(DEST_DIR).mkdirs();

        /* Duplicate the live database into the embedded resources directory. */
        Files.copy(Paths.get(SOURCE_DB), Paths.get(DEST_DB), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[OK] Copied bank.db → " + DEST_DB);

        /* Scrub Personally Identifiable Information (PII) from the bundled database. */
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DEST_DB);
             Statement  stmt = conn.createStatement()) {

            stmt.execute("UPDATE users SET full_name = 'User1', email = '', phone_number = '' WHERE customer_id = '07168386'");
            stmt.execute("UPDATE users SET full_name = 'User2', email = '', phone_number = '' WHERE customer_id = '35395655'");
            stmt.execute("UPDATE users SET full_name = 'User3', email = '', phone_number = '' WHERE customer_id = '69864325'");
            /* Retain administrator privileges while stripping contact details. */
            stmt.execute("UPDATE users SET email = '', phone_number = '' WHERE customer_id = 'admin'");

            System.out.println("[OK] User names → User1 / User2 / User3");
            System.out.println("[OK] Emails and phone numbers cleared");
        }

        System.out.println("\nDone! Next steps:");
        System.out.println("  1. Review " + DEST_DB + " in DB Browser to confirm");
        System.out.println("  2. Run: mvn package");
        System.out.println("  3. Upload target/MazeBank.exe to GitHub Releases");
        System.out.println("     (the transaction history is now bundled inside the EXE)");
    }
}
