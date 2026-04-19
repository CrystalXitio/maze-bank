package com.banking.controllers;

import com.banking.models.User;
import com.banking.services.BankingService;
import com.banking.utils.ThemeStyles;
import com.banking.views.LoginFrame;
import com.banking.views.DashboardFrame;
import com.banking.views.RegisterFrame;

public class LoginController {
    private LoginFrame loginFrame;
    private BankingService bankingService;

    public LoginController(LoginFrame loginFrame, BankingService bankingService) {
        this.loginFrame = loginFrame;
        this.bankingService = bankingService;

        this.loginFrame.getLoginButton().addActionListener(e -> attemptLogin());
        this.loginFrame.getRegisterButton().addActionListener(e -> navigateToRegister());
        this.loginFrame.getThemeToggleButton().addActionListener(e -> {
            ThemeStyles.toggleTheme();
            loginFrame.getThemeToggleButton().setText(ThemeStyles.isDarkMode() ? "☀" : "🌙");
        });
    }

    private void attemptLogin() {
        String customerId = loginFrame.getCustomerId();
        String password = loginFrame.getPassword();

        if (customerId.isEmpty() || password.isEmpty()) {
            loginFrame.setStatusMessage("Credentials cannot be empty.");
            return;
        }

        User user = bankingService.authenticate(customerId, password);

        if (user != null) {
            try {
                if (user.isAdmin()) {
                    com.banking.views.AdminDashboardFrame adminFrame = new com.banking.views.AdminDashboardFrame();
                    new com.banking.controllers.AdminDashboardController(adminFrame, bankingService, user);
                    adminFrame.setVisible(true);
                } else {
                    DashboardFrame dashboardFrame = new DashboardFrame();
                    new DashboardController(dashboardFrame, bankingService, user);
                    dashboardFrame.setVisible(true);
                }
                loginFrame.dispose();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        } else {
            loginFrame.setStatusMessage("Invalid Customer ID or password.");
        }
    }

    private void navigateToRegister() {
        RegisterFrame regFrame = new RegisterFrame();
        new RegisterController(regFrame, bankingService);
        regFrame.setVisible(true);
        loginFrame.dispose();
    }
}
