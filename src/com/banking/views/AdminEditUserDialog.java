package com.banking.views;

import com.banking.models.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class AdminEditUserDialog extends JDialog {
    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JButton saveButton;
    private JButton cancelButton;

    public AdminEditUserDialog(Frame parent, User user) {
        super(parent, "Edit User Details: " + user.getCustomerId(), true);
        setSize(400, 250);
        setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel(new MigLayout("wrap 2, insets 20", "[right][grow, fill]", "[]15[]10[]10[]20[]"));
        
        panel.add(new JLabel("Full Name:"));
        fullNameField = new JTextField(user.getFullName());
        panel.add(fullNameField);
        
        panel.add(new JLabel("Email Address:"));
        emailField = new JTextField(user.getEmail());
        panel.add(emailField);
        
        panel.add(new JLabel("Phone Number:"));
        phoneField = new JTextField(user.getPhoneNumber());
        panel.add(phoneField);
        
        saveButton = new JButton("Save Changes");
        saveButton.setBackground(new Color(0, 122, 255));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(cancelButton, "span 2, split 2, center");
        panel.add(saveButton);
        
        add(panel);
    }
    
    public String getFullName() { return fullNameField.getText(); }
    public String getEmail() { return emailField.getText(); }
    public String getPhone() { return phoneField.getText(); }
    public JButton getSaveButton() { return saveButton; }
    public JButton getCancelButton() { return cancelButton; }
}
