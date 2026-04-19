package com.banking.controllers;

import com.banking.models.Account;
import com.banking.models.Transaction;
import com.banking.models.Transaction.TransactionType;
import com.banking.models.User;
import com.banking.services.BankingService;
import com.banking.utils.ThemeStyles;
import com.banking.views.DashboardFrame;
import com.banking.views.LoginFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {
    private DashboardFrame dashboardFrame;
    private BankingService bankingService;
    private User currentUser;
    private Account selectedAccount;
    private Account ledgerAccount;
    
    private final DateTimeFormatter UI_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DashboardController(DashboardFrame dashboardFrame, BankingService bankingService, User user) {
        this.dashboardFrame = dashboardFrame;
        this.bankingService = bankingService;
        this.currentUser = user;
        
        initializeView();
        

        this.dashboardFrame.getAccountSelector().addActionListener(e -> {
            selectedAccount = (Account) dashboardFrame.getAccountSelector().getSelectedItem();
            updateDashboard();
        });
        
        this.dashboardFrame.getTransactionAccountSelector().addActionListener(e -> {
            ledgerAccount = (Account) dashboardFrame.getTransactionAccountSelector().getSelectedItem();
            updateLedgerTab();
        });


        this.dashboardFrame.getNavDashboardBtn().addActionListener(e -> {
            refreshUserSessionData();
            updateDashboard();
            dashboardFrame.showPanel("DASHBOARD");
            dashboardFrame.setActiveNavButton((JButton) e.getSource());
        });
        this.dashboardFrame.getNavTransferBtn().addActionListener(e -> {
            refreshUserSessionData();
            dashboardFrame.showPanel("TRANSFER");
            dashboardFrame.setActiveNavButton((JButton) e.getSource());
        });
        this.dashboardFrame.getNavTransactionsBtn().addActionListener(e -> {
            refreshUserSessionData();
            dashboardFrame.showPanel("TRANSACTIONS");
            dashboardFrame.setActiveNavButton((JButton) e.getSource());
            updateLedgerTab();
        });
        

        this.dashboardFrame.getMyProfileBtn().addActionListener(e -> {
            refreshUserSessionData();
            dashboardFrame.showPanel("PROFILE");
            dashboardFrame.setActiveNavButton(null);
            populateProfileForm();
        });
        this.dashboardFrame.getThemeToggleButton().addActionListener(e -> ThemeStyles.toggleTheme());
        this.dashboardFrame.getLogoutButton().addActionListener(e -> {
            LoginFrame loginFrame = new LoginFrame();
            new LoginController(loginFrame, bankingService);
            loginFrame.setVisible(true);
            dashboardFrame.dispose();
        });
        

        this.dashboardFrame.getTransferButton().addActionListener(e -> performTransfer());
        this.dashboardFrame.getProfSaveBtn().addActionListener(e -> updateProfile());
        this.dashboardFrame.getProfDeleteBtn().addActionListener(e -> deleteAccount());
    }

    private void refreshUserSessionData() {
        for (User u : bankingService.getAllSystemUsers()) {
            if (u.getCustomerId().equals(currentUser.getCustomerId())) {
                this.currentUser = u;
                break;
            }
        }
        dashboardFrame.setWelcomeMessage("Welcome, " + currentUser.getFullName().split(" ")[0]);
    }

    private void initializeView() {
        dashboardFrame.setWelcomeMessage("Welcome, " + currentUser.getFullName().split(" ")[0]);
        
        JComboBox<Account> selector = dashboardFrame.getAccountSelector();
        JComboBox<Account> ledgerSel = dashboardFrame.getTransactionAccountSelector();
        selector.removeAllItems();
        ledgerSel.removeAllItems();
        
        for (Account acc : currentUser.getAccounts()) {
            selector.addItem(acc);
            ledgerSel.addItem(acc);
        }
        
        if (!currentUser.getAccounts().isEmpty()) {
            selector.setSelectedIndex(0);
            ledgerSel.setSelectedIndex(0);
            selectedAccount = currentUser.getAccounts().get(0);
            ledgerAccount = currentUser.getAccounts().get(0);
        }
        
        updateDashboard();
        dashboardFrame.showPanel("DASHBOARD");
    }

    private void populateProfileForm() {
        dashboardFrame.setProfNameField(currentUser.getFullName());

        if (!currentUser.getAccounts().isEmpty()) {
            dashboardFrame.setProfAccountRefField(currentUser.getAccounts().get(0).getAccountId());
        }
        dashboardFrame.setProfEmailField(currentUser.getEmail());
        dashboardFrame.setProfPhoneField(currentUser.getPhoneNumber());
    }
    
    private void updateProfile() {
        String newEmail = dashboardFrame.getProfEmailField();
        String newPhone = dashboardFrame.getProfPhoneField();
        
        if (!newEmail.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            JOptionPane.showMessageDialog(dashboardFrame, "Email must be a valid @gmail.com address.", "Validation Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!newPhone.matches("^\\d{10}$")) {
            JOptionPane.showMessageDialog(dashboardFrame, "Phone number must be exactly 10 digits.", "Validation Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            bankingService.updateUserProfile(currentUser.getCustomerId(), newEmail, newPhone);
            currentUser.setEmail(newEmail);
            currentUser.setPhoneNumber(newPhone);
            JOptionPane.showMessageDialog(dashboardFrame, "Profile parameters updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dashboardFrame, "Database connection error.", "Validation Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(dashboardFrame,
            "Are you sure you want to permanently delete your account?\nThis action cannot be undone and all your data will be lost.",
            "Delete Account", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        int reconfirm = JOptionPane.showConfirmDialog(dashboardFrame,
            "This is your FINAL confirmation.\nAll accounts, transactions, and personal data will be permanently erased.",
            "Final Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (reconfirm != JOptionPane.YES_OPTION) return;
        
        try {
            bankingService.deleteUser(currentUser.getCustomerId());
            JOptionPane.showMessageDialog(dashboardFrame, "Your account has been permanently deleted.", "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
            LoginFrame loginFrame = new LoginFrame();
            new LoginController(loginFrame, bankingService);
            loginFrame.setVisible(true);
            dashboardFrame.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dashboardFrame, "Error deleting account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDashboard() {
        if (selectedAccount == null) return;
        
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);
        String accId = selectedAccount.getAccountId();
        
        double currentBalance = bankingService.getCurrentBalance(accId);
        dashboardFrame.setTotalBalanceValue("₹" + String.format("%,.2f", currentBalance));
        
        double expensesThisMonth = bankingService.calculateMonthlyTotal(accId, thisMonth, TransactionType.WITHDRAWAL);
        double expensesLastMonth = bankingService.calculateMonthlyTotal(accId, lastMonth, TransactionType.WITHDRAWAL);
        double expChange = bankingService.calculatePercentageChange(expensesThisMonth, expensesLastMonth);
        
        dashboardFrame.setMonthlyExpensesValue("₹" + String.format("%,.2f", expensesThisMonth));
        dashboardFrame.getMonthlyExpensesPercent().setText((expChange >= 0 ? "+" : "") + String.format("%.1f%%", expChange) + " vs last month");
        dashboardFrame.getMonthlyExpensesPercent().setForeground(expChange <= 0 ? new Color(40, 167, 69) : new Color(220, 53, 69));
        
        double depositsThisMonth = bankingService.calculateMonthlyTotal(accId, thisMonth, TransactionType.DEPOSIT);
        double depositsLastMonth = bankingService.calculateMonthlyTotal(accId, lastMonth, TransactionType.DEPOSIT);
        double depChange = bankingService.calculatePercentageChange(depositsThisMonth, depositsLastMonth);
        
        dashboardFrame.setTotalSavingsValue("₹" + String.format("%,.2f", depositsThisMonth));
        dashboardFrame.getTotalSavingsPercent().setText((depChange >= 0 ? "+" : "") + String.format("%.1f%%", depChange) + " vs last month");
        dashboardFrame.getTotalSavingsPercent().setForeground(depChange >= 0 ? new Color(40, 167, 69) : new Color(220, 53, 69));
        
        DefaultTableModel model = dashboardFrame.getRecentTxTableModel();
        model.setRowCount(0);
        
        List<Transaction> transactions = bankingService.getTransactions(accId);
        transactions.sort((a,b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        for (int i = 0; i < Math.min(5, transactions.size()); i++) {
            Transaction t = transactions.get(i);
            String sign = (t.getType() == TransactionType.DEPOSIT) ? "+" : "-";
            model.addRow(new Object[]{
                t.getTimestamp().format(UI_FORMATTER),
                sign + "₹" + String.format("%,.2f", t.getAmount()),
                t.getDescription()
            });
        }
    }
    
    private void updateLedgerTab() {
        if (ledgerAccount == null) return;
        DefaultTableModel model = dashboardFrame.getLedgerTableModel();
        model.setRowCount(0);
        
        String accId = ledgerAccount.getAccountId();
        List<Transaction> transactions = bankingService.getTransactions(accId);
        for (Transaction t : transactions) {
            String sign = (t.getType() == TransactionType.DEPOSIT) ? "+" : "-";
            model.addRow(new Object[]{
                t.getTimestamp().format(UI_FORMATTER),
                t.getType().name(),
                sign + "₹" + String.format("%,.2f", t.getAmount()),
                t.getDescription()
            });
        }
    }

    private void performTransfer() {
        if (selectedAccount == null) return;
        String amountStr = dashboardFrame.getTransferAmount();
        String destAccountId = dashboardFrame.getTransferDestination();
        
        try {
            double amount = Double.parseDouble(amountStr);
            if(destAccountId.equalsIgnoreCase("withdraw")) {
                 bankingService.withdraw(selectedAccount.getAccountId(), amount);
                 JOptionPane.showMessageDialog(dashboardFrame, "Withdrawal successful!");
            } else {
                Account destAccount = bankingService.getAccountById(destAccountId);
                if (destAccount == null) {
                    JOptionPane.showMessageDialog(dashboardFrame, "Destination account not found. Type 'withdraw' for cash.");
                    return;
                }
                if (destAccount.getAccountId().equals(selectedAccount.getAccountId())) {
                    JOptionPane.showMessageDialog(dashboardFrame, "Cannot transfer to the same account.");
                    return;
                }
                bankingService.transferFunds(selectedAccount.getAccountId(), destAccount.getAccountId(), amount);
                JOptionPane.showMessageDialog(dashboardFrame, "Transfer successful!");
            }
            
            dashboardFrame.clearTransferFields();
            updateDashboard();
            dashboardFrame.showPanel("DASHBOARD"); 
            dashboardFrame.setActiveNavButton(dashboardFrame.getNavDashboardBtn());
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dashboardFrame, "Invalid amount format.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(dashboardFrame, ex.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dashboardFrame, "Database error: " + e.getMessage());
        }
    }
}
