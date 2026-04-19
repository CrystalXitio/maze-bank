package com.banking;

import com.banking.controllers.LoginController;
import com.banking.services.BankingService;
import com.banking.utils.ThemeStyles;
import com.banking.views.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;

public class ApplicationRunner {
    public static void main(String[] args) {

        FlatLaf.registerCustomDefaultsSource("com.banking.themes");
        FlatDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            ThemeStyles.applyTheme(true);

            BankingService bankingService = new BankingService();

            LoginFrame loginFrame = new LoginFrame();
            new LoginController(loginFrame, bankingService);

            loginFrame.setVisible(true);
        });
    }
}
