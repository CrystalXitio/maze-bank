package com.banking.views;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField fullNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel statusLabel;

    public RegisterFrame() {
        setTitle("Maze Bank - Register");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Create an Account", SwingConstants.CENTER);
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 20f));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        
        formPanel.add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);
        
        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        backButton = new JButton("Back to Login");
        registerButton = new JButton("Register");
        buttonPanel.add(backButton);
        buttonPanel.add(registerButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    public String getFullName() { return fullNameField.getText(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public String getConfirmPassword() { return new String(confirmPasswordField.getPassword()); }
    public JButton getRegisterButton() { return registerButton; }
    public JButton getBackButton() { return backButton; }
    public void setStatusMessage(String message) { statusLabel.setText(message); }
}
