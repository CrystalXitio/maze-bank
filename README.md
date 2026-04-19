# 🏦 Maze Bank

A **Java Swing desktop banking application** built with FlatLaf and MigLayout. Maze Bank simulates a real-world retail banking platform with multi-role access — customers can manage checking and savings accounts, transfer funds, and view transaction history, while an admin panel provides full user and account oversight.

---

## ✨ Features

| Feature                    | Details                                                               |
| -------------------------- | --------------------------------------------------------------------- |
| 🔐 **Authentication**      | Secure login with Customer ID & password; new-user registration       |
| 👤 **Customer Dashboard**  | Deposit, withdraw, transfer between accounts, full transaction log    |
| 🏧 **Dual Accounts**       | Separate Checking & Savings accounts per user                         |
| 🗂️ **Transaction History** | Timestamped, filterable ledger of all activity                        |
| 🛡️ **Admin Panel**         | View all users, edit details, enable/disable accounts                 |
| 💾 **Persistent Storage**  | Data stored via Java Object Serialization — zero external DB required |
| 🎨 **Modern UI**           | Dark-mode Swing UI powered by FlatLaf with a custom Maze Bank theme   |

---

## 🗂️ Project Structure

```
maze-bank/
├── src/
│   └── com/banking/
│       ├── ApplicationRunner.java      ← Entry point
│       ├── controllers/                ← Event handlers
│       ├── models/                     ← User, Account, Transaction
│       ├── services/                   ← BankingService (core logic)
│       ├── themes/                     ← FlatLaf custom theme
│       ├── utils/                      ← Helper utilities
│       └── views/                      ← Swing frames & dialogs
│           ├── LoginFrame.java
│           ├── RegisterFrame.java
│           ├── DashboardFrame.java
│           ├── AdminDashboardFrame.java
│           └── AdminEditUserDialog.java
├── Logo.ico                            ← Application icon
└── pom.xml                             ← Maven build config
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 11+** — [Download](https://adoptium.net/)
- **Maven 3.6+** — [Download](https://maven.apache.org/)

### Build & Run

```bash
# Clone the repo
git clone https://github.com/CrystalXitio/maze-bank.git
cd maze-bank

# Compile & package a fat JAR
mvn package

# Run the JAR
java -jar target/smartbanking-2.0-SNAPSHOT.jar
```

### (Optional) Build the Windows EXE

The Maven build includes the **Launch4j** plugin, which automatically generates `MazeBank.exe` inside `target/` when you run `mvn package`.

```bash
mvn package
# → target/MazeBank.exe
```

---

## 🔑 Demo Credentials

| Name    | Customer ID | Password   |
| ------- | ----------- | ---------- |
| Krishna | `07168386`  | `1234567k` |
| Agrim   | `35395655`  | `1234567a` |
| Pranshu | `69864325`  | `1234567p` |

> **Admin access** is role-based — log in with an admin-enabled account.

---

## 🏗️ Tech Stack

| Layer        | Technology                                        |
| ------------ | ------------------------------------------------- |
| Language     | Java 11                                           |
| UI Framework | Java Swing                                        |
| Look & Feel  | [FlatLaf 3.4.1](https://www.formdev.com/flatlaf/) |
| Layout       | [MigLayout 11.3](https://www.miglayout.com/)      |
| Persistence  | Java Object Serialization                         |
| Build        | Apache Maven 3                                    |
| EXE Wrapper  | Launch4j (via Maven plugin)                       |

---

## 📄 License

This project is for educational and demonstration purposes.  
© 2026 Maze Bank. All rights reserved.
