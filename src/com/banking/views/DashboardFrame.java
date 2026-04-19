package com.banking.views;

import com.banking.models.Account;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Core user interface dashboard for the banking application.
 * Manages the presentation layer for account overviews, fund transfers, 
 * transaction ledgers, and user profile management.
 */
public class DashboardFrame extends JFrame {
    private JLabel welcomeLabel;
    
    private JLabel totalBalanceValue;
    private JLabel totalSavingsValue;
    private JLabel monthlyExpensesValue;
    
    private JLabel totalBalancePercent;
    private JLabel totalSavingsPercent;
    private JLabel monthlyExpensesPercent;

    private JComboBox<Account> accountSelector;
    private JComboBox<Account> transactionAccountSelector;
    

    private JTable recentTxTable;
    private DefaultTableModel recentTxTableModel;
    

    private JTable ledgerTable;
    private DefaultTableModel ledgerTableModel;
    
    private JTextField transferAmountField;
    private JTextField transferDestinationField;
    private JButton transferButton;
    

    private JTextField profNameField;
    private JTextField profAccountRefField;
    private JPasswordField profPasswordField;
    private JTextField profEmailField;
    private JTextField profPhoneField;
    private JButton profSaveBtn;
    private JButton profDeleteBtn;
    
    private JButton navDashboardBtn;
    private JButton navTransferBtn;
    private JButton navTransactionsBtn;
    

    private JButton myProfileBtn;
    private JButton logoutButton;
    private JButton themeToggleButton;
    
    private JPanel routerPanel;
    private CardLayout cardLayout;

    public DashboardFrame() {
        setTitle("Maze Bank - User Portal");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());


        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(new Color(20, 20, 20));
        topHeader.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        JLabel brandTopLabel = new JLabel("Maze Bank Portal");
        brandTopLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandTopLabel.setForeground(Color.WHITE);
        
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        headerActions.setOpaque(false);
        
        themeToggleButton = createHeaderButton("Toggle Theme", new Color(45, 45, 45));
        myProfileBtn = createHeaderButton("My Profile", new Color(0, 122, 255));
        logoutButton = createHeaderButton("Logout", new Color(220, 53, 69));
        
        headerActions.add(themeToggleButton);
        headerActions.add(myProfileBtn);
        headerActions.add(logoutButton);
        
        topHeader.add(brandTopLabel, BorderLayout.WEST);
        topHeader.add(headerActions, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);


        JPanel sidebar = new JPanel(new MigLayout("wrap 1, fillx, insets 30 20 20 20", "[grow]", "[]20[]30[]10[]10[]10[]push"));
        sidebar.setBackground(new Color(30, 30, 30));
        sidebar.setPreferredSize(new Dimension(250, 0));
        
        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        sidebar.add(welcomeLabel, "align center, wrap");

        navDashboardBtn = createSidebarButton("Dashboard");
        navTransferBtn = createSidebarButton("Transfer Funds");
        navTransactionsBtn = createSidebarButton("Full Ledger");
        
        sidebar.add(navDashboardBtn, "growx");
        sidebar.add(navTransferBtn, "growx");
        sidebar.add(navTransactionsBtn, "growx");
        
        add(sidebar, BorderLayout.WEST);


        cardLayout = new CardLayout();
        routerPanel = new JPanel(cardLayout) {
             @Override
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                 g.setColor(UIManager.getColor("Panel.background"));
                 g.fillRect(0, 0, getWidth(), getHeight());
             }
        };


        JPanel dashboardView = new JPanel(new MigLayout("fill, insets 30", "[grow]", "[][][grow]"));
        dashboardView.setOpaque(false);

        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        headerPanel.setOpaque(false);
        JLabel dashboardTitle = new JLabel("Account Overview");
        dashboardTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        accountSelector = new JComboBox<>();
        accountSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        headerPanel.add(dashboardTitle, "split 2, left");
        headerPanel.add(accountSelector, "gapleft 20");
        dashboardView.add(headerPanel, "growx, wrap 20");

        JPanel cardsPanel = new JPanel(new MigLayout("insets 0, gap 20", "[grow][grow][grow]", "[]"));
        cardsPanel.setOpaque(false);
        
        totalBalanceValue = new JLabel("₹0.00");
        totalBalancePercent = new JLabel("+0.0% vs last month");
        JPanel balanceCard = createSummaryCard("Total Balance", totalBalanceValue, totalBalancePercent);
        
        totalSavingsValue = new JLabel("₹0.00");
        totalSavingsPercent = new JLabel("+0.0% vs last month");
        JPanel savingsCard = createSummaryCard("Total Deposits", totalSavingsValue, totalSavingsPercent);
        
        monthlyExpensesValue = new JLabel("₹0.00");
        monthlyExpensesPercent = new JLabel("+0.0% vs last month");
        JPanel expensesCard = createSummaryCard("Monthly Expenses", monthlyExpensesValue, monthlyExpensesPercent);

        cardsPanel.add(balanceCard, "grow");
        cardsPanel.add(savingsCard, "grow");
        cardsPanel.add(expensesCard, "grow");
        dashboardView.add(cardsPanel, "growx, wrap");

        /* Initialize restricted-view transaction ledger for the primary dashboard component. */
        recentTxTableModel = new DefaultTableModel(new String[]{"Date", "Amount", "Description"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        recentTxTable = new JTable(recentTxTableModel);
        recentTxTable.setRowHeight(35);
        applyColorRenderer(recentTxTable, 1);
        
        recentTxTable.getTableHeader().setReorderingAllowed(false);
        
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setOpaque(false);
        JLabel recentLabel = new JLabel("Recent Activity");
        recentLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recentLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        recentPanel.add(recentLabel, BorderLayout.NORTH);
        recentPanel.add(new JScrollPane(recentTxTable), BorderLayout.CENTER);
        
        dashboardView.add(recentPanel, "grow, wrap");
        routerPanel.add(dashboardView, "DASHBOARD");


        JPanel transferView = new JPanel(new MigLayout("fill, center", "[center]", "push[]push"));
        transferView.setOpaque(false);
        
        JPanel transferBox = new JPanel(new MigLayout("wrap 1, insets 40 50 40 50, center", "[400!]", "[]20[]10[]20[]10[]30[]"));
        transferBox.setBackground(UIManager.getColor("Panel.background"));
        transferBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel transferHeader = new JLabel("Make a Transfer");
        transferHeader.setFont(new Font("Segoe UI", Font.BOLD, 28));
        
        JLabel amtLabel = new JLabel("Amount (₹)");
        amtLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transferAmountField = new JTextField();
        transferAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        
        JLabel destLabel = new JLabel("Destination Account ID (or 'withdraw')");
        destLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transferDestinationField = new JTextField();
        transferDestinationField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        
        transferButton = new JButton("Execute Transfer");
        transferButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        transferButton.setBackground(new Color(0, 122, 255));
        transferButton.setForeground(Color.WHITE);
        transferButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        transferButton.setFocusPainted(false);
        
        transferBox.add(transferHeader, "center, wrap 30");
        transferBox.add(amtLabel, "left");
        transferBox.add(transferAmountField, "growx, height 40!");
        transferBox.add(destLabel, "left");
        transferBox.add(transferDestinationField, "growx, height 40!");
        transferBox.add(transferButton, "growx, height 50!");

        transferView.add(transferBox);
        routerPanel.add(transferView, "TRANSFER");


        JPanel ledgerView = new JPanel(new MigLayout("fill, insets 30", "[grow]", "[][grow]"));
        ledgerView.setOpaque(false);
        
        JPanel ledgerHeader = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        ledgerHeader.setOpaque(false);
        JLabel ledgerTitle = new JLabel("Master Transaction Ledger");
        ledgerTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        transactionAccountSelector = new JComboBox<>();
        transactionAccountSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        ledgerHeader.add(ledgerTitle, "split 2, left");
        ledgerHeader.add(transactionAccountSelector, "gapleft 20");
        ledgerView.add(ledgerHeader, "growx, wrap 20");
        
        ledgerTableModel = new DefaultTableModel(new String[]{"Date", "Type", "Amount", "Description"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        ledgerTable = new JTable(ledgerTableModel);
        ledgerTable.setRowHeight(35);
        applyColorRenderer(ledgerTable, 2);
        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(ledgerTableModel);
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
        sorter.setComparator(2, new java.util.Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    double d1 = Double.parseDouble(s1.replaceAll("[^\\d.-]", ""));
                    double d2 = Double.parseDouble(s2.replaceAll("[^\\d.-]", ""));
                    if(s1.startsWith("-") && d1 > 0) d1 = -d1; 
                    if(s2.startsWith("-") && d2 > 0) d2 = -d2;
                    return Double.compare(d1, d2);
                } catch(Exception e) { return 0; }
            }
        });
        ledgerTable.setRowSorter(sorter);
        
        ledgerView.add(new JScrollPane(ledgerTable), "grow");
        routerPanel.add(ledgerView, "TRANSACTIONS");


        JPanel profileView = new JPanel(new MigLayout("fill, center", "[center]", "push[]push"));
        profileView.setOpaque(false);
        
        JPanel profBox = new JPanel(new MigLayout("wrap 2, insets 40, gapy 15", "[right][400!]", "[]30[][][][]20[][]30[]"));
        profBox.setBackground(UIManager.getColor("Panel.background"));
        profBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel profTitle = new JLabel("My Profile Settings");
        profTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        profBox.add(profTitle, "span 2, center");
        

        profBox.add(new JLabel("Full Name:"));
        profNameField = new JTextField();
        profNameField.setEditable(false);
        profBox.add(profNameField, "growx, height 35!");
        
        profBox.add(new JLabel("Primary Account ID:"));
        profAccountRefField = new JTextField();
        profAccountRefField.setEditable(false);
        profBox.add(profAccountRefField, "growx, height 35!");
        
        profBox.add(new JLabel("Password:"));
        profPasswordField = new JPasswordField("********");
        profPasswordField.setEditable(false);
        profBox.add(profPasswordField, "growx, height 35!");
        

        profBox.add(new JLabel("Contact Email:"), "gaptop 20");
        profEmailField = new JTextField();
        profBox.add(profEmailField, "growx, height 35!, gaptop 20");
        
        profBox.add(new JLabel("Phone Number:"));
        profPhoneField = new JTextField();
        profBox.add(profPhoneField, "growx, height 35!");
        
        profSaveBtn = new JButton("Save Profile");
        profSaveBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        profSaveBtn.setBackground(new Color(0, 122, 255));
        profSaveBtn.setForeground(Color.WHITE);
        profSaveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        profDeleteBtn = new JButton("Delete My Account");
        profDeleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profDeleteBtn.setBackground(new Color(220, 53, 69));
        profDeleteBtn.setForeground(Color.WHITE);
        profDeleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profDeleteBtn.setFocusPainted(false);
        
        profBox.add(profSaveBtn, "span 2, growx, height 45!");
        profBox.add(profDeleteBtn, "span 2, growx, height 38!");
        
        profileView.add(profBox);
        routerPanel.add(profileView, "PROFILE");

        add(routerPanel, BorderLayout.CENTER);
        setActiveNavButton(navDashboardBtn);
    }
    
    private void applyColorRenderer(JTable table, int colIndex) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && !isSelected) {
                    String str = value.toString();
                    if (str.startsWith("+")) {
                        c.setForeground(new Color(40, 167, 69));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (str.startsWith("-")) {
                        c.setForeground(new Color(220, 53, 69));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(UIManager.getColor("Table.foreground"));
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                } else if (!isSelected) {
                   c.setForeground(UIManager.getColor("Table.foreground"));
                   c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(45, 45, 45));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        return btn;
    }
    
    private JButton createHeaderButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }
    
    public void setActiveNavButton(JButton activeBtn) {
        JButton[] navs = {navDashboardBtn, navTransferBtn, navTransactionsBtn};
        for (JButton btn : navs) {
            btn.setBackground(new Color(45, 45, 45));
            btn.setBorderPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        }
        if (activeBtn != null) {
            activeBtn.setBackground(new Color(65, 65, 65));
            activeBtn.setBorderPainted(true);
            activeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(0, 122, 255)),
                BorderFactory.createEmptyBorder(10, 15, 10, 10)
            ));
        }
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, JLabel percentLabel) {
        JPanel card = new JPanel(new MigLayout("insets 20", "[grow]", "[]10[]5[]")) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(UIManager.getColor("Panel.background"));
                Color borderColor = UIManager.getColor("Component.borderColor");
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor != null ? borderColor : new Color(220, 220, 220), 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        };
        card.setBackground(UIManager.getColor("Panel.background"));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        percentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(titleLabel, "wrap");
        card.add(valueLabel, "wrap");
        card.add(percentLabel, "");
        
        return card;
    }

    public void showPanel(String cardName) {
        cardLayout.show(routerPanel, cardName);
    }

    public void setWelcomeMessage(String msg) { welcomeLabel.setText(msg); }
    public void setTotalBalanceValue(String msg) { totalBalanceValue.setText(msg); }
    public void setTotalSavingsValue(String msg) { totalSavingsValue.setText(msg); }
    public void setMonthlyExpensesValue(String msg) { monthlyExpensesValue.setText(msg); }
    
    public JLabel getTotalBalancePercent() { return totalBalancePercent; }
    public JLabel getTotalSavingsPercent() { return totalSavingsPercent; }
    public JLabel getMonthlyExpensesPercent() { return monthlyExpensesPercent; }
    
    public DefaultTableModel getRecentTxTableModel() { return recentTxTableModel; }
    public DefaultTableModel getLedgerTableModel() { return ledgerTableModel; }
    
    public JComboBox<Account> getAccountSelector() { return accountSelector; }
    public JComboBox<Account> getTransactionAccountSelector() { return transactionAccountSelector; }
    

    public void setProfNameField(String val) { profNameField.setText(val); }
    public void setProfAccountRefField(String val) { profAccountRefField.setText(val); }
    public String getProfEmailField() { return profEmailField.getText(); }
    public void setProfEmailField(String val) { profEmailField.setText(val); }
    public String getProfPhoneField() { return profPhoneField.getText(); }
    public void setProfPhoneField(String val) { profPhoneField.setText(val); }
    
    public JButton getProfSaveBtn() { return profSaveBtn; }
    public JButton getProfDeleteBtn() { return profDeleteBtn; }
    
    public JButton getNavDashboardBtn() { return navDashboardBtn; }
    public JButton getNavTransferBtn() { return navTransferBtn; }
    public JButton getNavTransactionsBtn() { return navTransactionsBtn; } 
    
    public JButton getMyProfileBtn() { return myProfileBtn; }
    public JButton getTransferButton() { return transferButton; }
    public JButton getLogoutButton() { return logoutButton; }
    public JButton getThemeToggleButton() { return themeToggleButton; }
    
    public String getTransferAmount() { return transferAmountField.getText(); }
    public String getTransferDestination() { return transferDestinationField.getText(); }
    public void clearTransferFields() { 
        transferAmountField.setText("");
        transferDestinationField.setText("");
    }
}
