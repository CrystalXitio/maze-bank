package com.banking.controllers;

import com.banking.services.BankingService;
import com.banking.views.LoginFrame;
import com.banking.views.RegisterFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterController {
    private RegisterFrame registerFrame;
    private BankingService bankingService;

    public RegisterController(RegisterFrame registerFrame, BankingService bankingService) {
        this.registerFrame = registerFrame;
        this.bankingService = bankingService;

        this.registerFrame.getRegisterButton().addActionListener(new RegisterAction());
        this.registerFrame.getBackButton().addActionListener(e -> navigateToLogin());
    }

    private void navigateToLogin() {
        LoginFrame loginFrame = new LoginFrame();
        new LoginController(loginFrame, bankingService);
        loginFrame.setVisible(true);
        registerFrame.dispose();
    }

    class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fullName = registerFrame.getFullName();
            String password = registerFrame.getPassword();
            String confirm = registerFrame.getConfirmPassword();

            if (fullName.trim().isEmpty() || password.isEmpty()) {
                registerFrame.setStatusMessage("Fields cannot be empty.");
                return;
            }

            if (!password.equals(confirm)) {
                registerFrame.setStatusMessage("Passwords do not match.");
                return;
            }

            if (!bankingService.validatePassword(password)) {
                registerFrame.setStatusMessage("Pass >=8 chars, 1 letter, 1 digit.");
                return;
            }

            String newCustomerId = bankingService.registerUser(fullName, password);
            if (newCustomerId != null) {
                JOptionPane.showMessageDialog(registerFrame, 
                    "Registration Complete!\n\nYour permanent Customer ID is: " + newCustomerId + "\n\nPlease write this down. You will need it to log in.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
                navigateToLogin();
            } else {
                registerFrame.setStatusMessage("An error occurred during registration.");
            }
        }
    }
}
