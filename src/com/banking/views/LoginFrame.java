package com.banking.views;

import javax.swing.*;
import java.awt.*;
import com.banking.utils.ThemeStyles;

public class LoginFrame extends JFrame {
    private JTextField customerIdField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton themeToggleButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Maze Bank - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));


        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Maze Bank", SwingConstants.CENTER);
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 24f));
        themeToggleButton = new JButton(ThemeStyles.isDarkMode() ? "☀" : "🌙");
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(themeToggleButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);


        JPanel formPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        formPanel.add(new JLabel("Customer ID:"));
        customerIdField = new JTextField();
        formPanel.add(customerIdField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);


        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(UIManager.getColor("nimbusRed"));
        footerPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        footerPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public String getCustomerId() { return customerIdField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public JButton getLoginButton() { return loginButton; }
    public JButton getRegisterButton() { return registerButton; }
    public JButton getThemeToggleButton() { return themeToggleButton; }
    public void setStatusMessage(String message) { statusLabel.setText(message); }
}
