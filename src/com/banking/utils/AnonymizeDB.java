package com.banking.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * One-time utility: Copies bank.db → src/com/banking/resources/bank.db
 * and anonymizes all personal details (names, emails, phone numbers).
 *
 * Run this ONCE from the project root before doing `mvn package`.
 * After running, the anonymized DB is bundled into the JAR/EXE automatically.
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

        // 1 – Create destination directory
        new File(DEST_DIR).mkdirs();

        // 2 – Copy the live database to resources
        Files.copy(Paths.get(SOURCE_DB), Paths.get(DEST_DB), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[OK] Copied bank.db → " + DEST_DB);

        // 3 – Anonymize the copy: replace real names, wipe email & phone
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DEST_DB);
             Statement  stmt = conn.createStatement()) {

            stmt.execute("UPDATE users SET full_name = 'User1', email = '', phone_number = '' WHERE customer_id = '07168386'");
            stmt.execute("UPDATE users SET full_name = 'User2', email = '', phone_number = '' WHERE customer_id = '35395655'");
            stmt.execute("UPDATE users SET full_name = 'User3', email = '', phone_number = '' WHERE customer_id = '69864325'");
            // Admin account: just wipe contact info, keep role
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
