package com.banking.controllers;

import com.banking.models.Account;
import com.banking.models.User;
import com.banking.services.BankingService;
import com.banking.utils.ThemeStyles;
import com.banking.views.AdminDashboardFrame;
import com.banking.views.LoginFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class AdminDashboardController {
    private AdminDashboardFrame adminFrame;
    private BankingService bankingService;
    private User adminUser;

    public AdminDashboardController(AdminDashboardFrame adminFrame, BankingService bankingService, User adminUser) {
        this.adminFrame = adminFrame;
        this.bankingService = bankingService;
        this.adminUser = adminUser;

        seedUsersData();
        seedAuditData();

        this.adminFrame.getLogoutBtn().addActionListener(e -> {
            LoginFrame loginFrame = new LoginFrame();
            new LoginController(loginFrame, bankingService);
            loginFrame.setVisible(true);
            adminFrame.dispose();
        });

        this.adminFrame.getToggleSuspendBtn().addActionListener(e -> toggleSuspension());
        this.adminFrame.getExecuteOverrideBtn().addActionListener(e -> executeOverride());
        this.adminFrame.getAddUserBtn().addActionListener(e -> addUser());
        this.adminFrame.getDeleteUserBtn().addActionListener(e -> deleteSelectedUser());
        this.adminFrame.getThemeToggleBtn().addActionListener(e -> ThemeStyles.toggleTheme());
        

        this.adminFrame.getUsersTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JTable table = (JTable) e.getSource();
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row != -1 && col == 1) {
                    openEditDialog(table.convertRowIndexToModel(row));
                }
            }
        });
        
        this.adminFrame.getTargetUserDropdown().addActionListener(e -> {
            JComboBox<String> targetAccountCombo = adminFrame.getTargetAccountDropdown();
            targetAccountCombo.removeAllItems();
            String selectedUserStr = (String) adminFrame.getTargetUserDropdown().getSelectedItem();
            if (selectedUserStr != null && selectedUserStr.contains(": ")) {
                String customerId = selectedUserStr.split(": ")[1];
                for (User u : bankingService.getAllSystemUsers()) {
                    if (u.getCustomerId().equals(customerId)) {
                        for (Account acc : u.getAccounts()) {
                            targetAccountCombo.addItem(acc.getAccountType() + ": " + acc.getAccountId());
                        }
                        break;
                    }
                }
            }
        });
        
        this.adminFrame.getTabbedPane().addChangeListener(e -> {
            int idx = adminFrame.getTabbedPane().getSelectedIndex();
            if (idx == 0) {
                seedUsersData();
            } else if (idx == 1) {
                seedAuditData();
            }
        });
    }

    private void seedUsersData() {
        DefaultTableModel model = adminFrame.getUsersTableModel();
        model.setRowCount(0);
        
        JComboBox<String> userCombo = adminFrame.getTargetUserDropdown();
        String currentSelection = (String) userCombo.getSelectedItem();
        userCombo.removeAllItems();
        userCombo.addItem("Select a User...");
        
        
        List<User> allUsers = bankingService.getAllSystemUsers();
        for (User u : allUsers) {
            if (u.isAdmin()) continue;
            
            double totalBal = 0;
            for (Account acc : u.getAccounts()) {
                totalBal += acc.getBalance();
            }
            
            model.addRow(new Object[]{
                u.getCustomerId(),
                u.getFullName(),
                u.getEmail(),
                u.getPhoneNumber(),
                "₹" + String.format("%,.2f", totalBal),
                u.isSuspended() ? "FROZEN" : "ACTIVE"
            });
            userCombo.addItem(u.getFullName() + ": " + u.getCustomerId());
        }
        if (currentSelection != null) userCombo.setSelectedItem(currentSelection);
    }

    private void seedAuditData() {
        DefaultTableModel model = adminFrame.getAuditTableModel();
        model.setRowCount(0);
        
        List<Object[]> ledger = bankingService.getAllSystemTransactions();
        for (Object[] row : ledger) {
            String ts = (String) row[0];
            String cId = (String) row[1];
            String aId = (String) row[2];
            String type = (String) row[3];
            double amt = (Double) row[4];
            String desc = (String) row[5];
            
            String sign = type.equals("DEPOSIT") ? "+" : "-";
            model.addRow(new Object[]{
                ts, cId, aId, type, 
                sign + "₹" + String.format("%,.2f", amt),
                desc
            });
        }
    }

    private void toggleSuspension() {
        JTable table = adminFrame.getUsersTable();
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(adminFrame, "Please select a user from the table first.");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(selected);
        String customerId = (String) adminFrame.getUsersTableModel().getValueAt(modelRow, 0);
        String status = (String) adminFrame.getUsersTableModel().getValueAt(modelRow, 5);
        
        boolean isCurrentlySuspended = status.equals("FROZEN");
        
        int confirm = JOptionPane.showConfirmDialog(adminFrame, 
            "Are you sure you want to " + (isCurrentlySuspended ? "UNFREEZE" : "FREEZE") + " user " + customerId + "?", 
            "Confirm Action", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bankingService.toggleUserSuspension(customerId, !isCurrentlySuspended);
                seedUsersData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(adminFrame, "Database error: " + ex.getMessage());
            }
        }
    }

    private void executeOverride() {
        String selectedAccountStr = (String) adminFrame.getTargetAccountDropdown().getSelectedItem();
        String amountStr = adminFrame.getOverrideAmount();
        String reason = adminFrame.getOverrideReason();
        
        if (selectedAccountStr == null || selectedAccountStr.isEmpty() || amountStr.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(adminFrame, "All fields are required and an account must be selected.");
            return;
        }
        
        String targetAccountId = selectedAccountStr.split(": ")[1];
        
        try {
            double amount = Double.parseDouble(amountStr);
            bankingService.forceBalanceAdjustment(targetAccountId, amount, reason);
            
            JOptionPane.showMessageDialog(adminFrame, "Adjustment applied successfully.");
            adminFrame.clearOverrideForm();
            
            seedUsersData();
            seedAuditData();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(adminFrame, "Invalid amount format.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(adminFrame, "Error: " + e.getMessage());
        }
    }

    private void openEditDialog(int modelRow) {
        String customerId = (String) adminFrame.getUsersTableModel().getValueAt(modelRow, 0);
        
        User targetUser = null;
        for (User u : bankingService.getAllSystemUsers()) {
            if (u.getCustomerId().equals(customerId)) {
                targetUser = u;
                break;
            }
        }
        
        if (targetUser != null) {
            com.banking.views.AdminEditUserDialog dialog = new com.banking.views.AdminEditUserDialog(adminFrame, targetUser);
            dialog.getSaveButton().addActionListener(e -> {
                String newName = dialog.getFullName();
                String newEmail = dialog.getEmail();
                String newPhone = dialog.getPhone();
                
                if (!newEmail.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
                    JOptionPane.showMessageDialog(dialog, "Email must be a valid @gmail.com address.", "Validation Failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (!newPhone.matches("^\\d{10}$")) {
                    JOptionPane.showMessageDialog(dialog, "Phone number must be exactly 10 digits.", "Validation Failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    bankingService.adminUpdateUserExtendedInfo(customerId, newName, newEmail, newPhone);
                    dialog.dispose();
                    seedUsersData();
                    JOptionPane.showMessageDialog(adminFrame, "User details updated securely!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Failed to update: " + ex.getMessage());
                }
            });
            dialog.getCancelButton().addActionListener(e -> dialog.dispose());
            dialog.setVisible(true);
        }
    }

    private void addUser() {
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        Object[] fields = {
            "Full Name:", nameField,
            "Password (min 8 chars, letters + digits):", passField
        };
        int result = JOptionPane.showConfirmDialog(adminFrame, fields, "Add New User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        String pass = new String(passField.getPassword());

        if (name.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(adminFrame, "Name and password cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!bankingService.validatePassword(pass)) {
            JOptionPane.showMessageDialog(adminFrame, "Password must be at least 8 characters and contain both letters and digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newId = bankingService.registerUser(name, pass);
        if (newId != null) {
            JOptionPane.showMessageDialog(adminFrame, "User created!\nCustomer ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
            seedUsersData();
        } else {
            JOptionPane.showMessageDialog(adminFrame, "Failed to create user.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        JTable table = adminFrame.getUsersTable();
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(adminFrame, "Please select a user from the table first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        String customerId = (String) adminFrame.getUsersTableModel().getValueAt(modelRow, 0);
        String name = (String) adminFrame.getUsersTableModel().getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(adminFrame,
            "Permanently delete user " + name + " (" + customerId + ") and all their data?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            bankingService.deleteUser(customerId);
            JOptionPane.showMessageDialog(adminFrame, "User " + name + " has been permanently deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            seedUsersData();
            seedAuditData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(adminFrame, "Error: " + ex.getMessage());
        }
    }
}
