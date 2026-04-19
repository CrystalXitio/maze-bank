package com.banking.views;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {
    private JTabbedPane tabbedPane;
    

    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JButton toggleSuspendBtn;
    private JButton addUserBtn;
    private JButton deleteUserBtn;
    

    private JTable auditTable;
    private DefaultTableModel auditTableModel;
    

    private JComboBox<String> targetUserDropdown;
    private JComboBox<String> targetAccountDropdown;
    private JTextField overrideAmountField;
    private JTextField overrideReasonField;
    private JButton executeOverrideBtn;
    
    private JButton logoutBtn;
    private JButton themeToggleBtn;

    public AdminDashboardFrame() {
        setTitle("Maze Bank - Admin Suite");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(20, 20, 20));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("System Admin Console");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        themeToggleBtn = new JButton("Toggle Theme");
        themeToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn = new JButton("Logout");
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerBtns.setOpaque(false);
        headerBtns.add(themeToggleBtn);
        headerBtns.add(logoutBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(headerBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 16));


        JPanel userTab = new JPanel(new BorderLayout(10, 10));
        userTab.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        usersTableModel = new DefaultTableModel(new String[]{"Customer ID", "Full Name", "Email", "Phone", "Total Balance (₹)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(usersTableModel);
        usersTable.setRowHeight(30);
        usersTable.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                 Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 if (value != null) {
                     String status = value.toString();
                     if (status.equals("FROZEN")) {
                         c.setForeground(new Color(220, 53, 69));
                         c.setFont(c.getFont().deriveFont(Font.BOLD));
                     } else {
                         c.setForeground(new Color(40, 167, 69));
                         c.setFont(c.getFont().deriveFont(Font.PLAIN));
                     }
                 }
                 return c;
             }
        });
        usersTable.setRowSorter(new TableRowSorter<>(usersTableModel));

        JScrollPane userScroll = new JScrollPane(usersTable);
        
        JPanel userActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toggleSuspendBtn = new JButton("Freeze / Unfreeze Selected");
        toggleSuspendBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        addUserBtn = new JButton("+ Add User");
        addUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addUserBtn.setBackground(new Color(40, 167, 69));
        addUserBtn.setForeground(Color.WHITE);
        addUserBtn.setFocusPainted(false);
        
        deleteUserBtn = new JButton("Delete Selected");
        deleteUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteUserBtn.setBackground(new Color(220, 53, 69));
        deleteUserBtn.setForeground(Color.WHITE);
        deleteUserBtn.setFocusPainted(false);
        
        userActions.add(addUserBtn);
        userActions.add(deleteUserBtn);
        userActions.add(toggleSuspendBtn);
        
        userTab.add(userScroll, BorderLayout.CENTER);
        userTab.add(userActions, BorderLayout.SOUTH);
        tabbedPane.addTab("User Management", userTab);


        JPanel auditTab = new JPanel(new BorderLayout());
        auditTab.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        auditTableModel = new DefaultTableModel(new String[]{"Date", "Customer ID", "Account ID", "Type", "Amount", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        auditTable = new JTable(auditTableModel);
        auditTable.setRowHeight(30);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(auditTableModel);
        

        sorter.setComparator(4, new java.util.Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    double d1 = Double.parseDouble(s1.replaceAll("[^\\d.-]", ""));
                    double d2 = Double.parseDouble(s2.replaceAll("[^\\d.-]", ""));
                    return Double.compare(d1, d2);
                } catch(Exception e) { return 0; }
            }
        });

        sorter.setComparator(0, new java.util.Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    java.time.LocalDate d1 = java.time.LocalDate.parse(s1, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    java.time.LocalDate d2 = java.time.LocalDate.parse(s2, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    return d1.compareTo(d2);
                } catch(Exception e) { return s1.compareTo(s2); }
            }
        });
        
        auditTable.setRowSorter(sorter);
        auditTab.add(new JScrollPane(auditTable), BorderLayout.CENTER);
        tabbedPane.addTab("System Audit Ledger", auditTab);


        JPanel overrideTab = new JPanel(new MigLayout("wrap 1, insets 40, center", "[400!]", "[]20[]5[]15[]5[]15[]5[]20[]"));
        JLabel overrideTitle = new JLabel("Database Balance Adjustment");
        overrideTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        targetUserDropdown = new JComboBox<>();
        targetUserDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        targetAccountDropdown = new JComboBox<>();
        targetAccountDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        overrideAmountField = new JTextField();
        overrideAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        overrideReasonField = new JTextField();
        overrideReasonField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        executeOverrideBtn = new JButton("FORCE ADJUSTMENT");
        executeOverrideBtn.setBackground(new Color(220, 53, 69));
        executeOverrideBtn.setForeground(Color.WHITE);
        executeOverrideBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        overrideTab.add(overrideTitle, "center, wrap");
        overrideTab.add(new JLabel("Target User:"));
        overrideTab.add(targetUserDropdown, "growx, height 35!");
        overrideTab.add(new JLabel("Target Account:"));
        overrideTab.add(targetAccountDropdown, "growx, height 35!");
        overrideTab.add(new JLabel("Adjustment Amount (+ or -):"));
        overrideTab.add(overrideAmountField, "growx, height 35!");
        overrideTab.add(new JLabel("Override Reason (e.g., 'Fee Refund'):"));
        overrideTab.add(overrideReasonField, "growx, height 35!");
        overrideTab.add(executeOverrideBtn, "growx, height 45!");
        
        tabbedPane.addTab("Manual Overrides", overrideTab);

        add(tabbedPane, BorderLayout.CENTER);
    }


    public JTabbedPane getTabbedPane() { return tabbedPane; }
    public DefaultTableModel getUsersTableModel() { return usersTableModel; }
    public JTable getUsersTable() { return usersTable; }
    public DefaultTableModel getAuditTableModel() { return auditTableModel; }
    public JButton getToggleSuspendBtn() { return toggleSuspendBtn; }
    public JButton getAddUserBtn() { return addUserBtn; }
    public JButton getDeleteUserBtn() { return deleteUserBtn; }
    public JButton getLogoutBtn() { return logoutBtn; }
    public JButton getThemeToggleBtn() { return themeToggleBtn; }

    public JComboBox<String> getTargetUserDropdown() { return targetUserDropdown; }
    public JComboBox<String> getTargetAccountDropdown() { return targetAccountDropdown; }
    public String getOverrideAmount() { return overrideAmountField.getText(); }
    public String getOverrideReason() { return overrideReasonField.getText(); }
    public JButton getExecuteOverrideBtn() { return executeOverrideBtn; }
    
    public void clearOverrideForm() {
        if(targetUserDropdown.getItemCount() > 0) targetUserDropdown.setSelectedIndex(0);
        targetAccountDropdown.removeAllItems();
        overrideAmountField.setText("");
        overrideReasonField.setText("");
    }
}
