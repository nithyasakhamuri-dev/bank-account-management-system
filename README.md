# Bank Account Management System

A console-based banking application built with **Java, JDBC, and MySQL** that supports account creation, deposits, withdrawals, and secure inter-account transfers with full transaction history tracking.

## Overview

This project simulates the core backend logic of a simple banking system — the kind of CRUD + business-logic problem commonly used to demonstrate database-backed application design. It focuses on writing clean, defensive Java code around a relational database rather than just in-memory logic.

## Features

- **Create accounts** with an opening balance
- **Deposit / Withdraw** funds with balance validation
- **Transfer funds** between two accounts safely, using a real database transaction (commit/rollback) so a transfer can never leave money "stuck" between accounts if something fails mid-way
- **Transaction history** — every deposit, withdrawal, and transfer is logged with a timestamp
- **List all accounts** in the system

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Database | MySQL |
| DB Connectivity | JDBC (PreparedStatements throughout) |
| IDE | IntelliJ IDEA |

## Design Notes

- **DAO pattern** — `BankDAO.java` keeps all SQL isolated from the console/menu logic in `Main.java`.
- **`BigDecimal` for money** — avoids the floating-point rounding errors `double`/`float` would introduce with currency.
- **Parameterized queries everywhere** — every SQL statement uses `PreparedStatement`, preventing SQL injection.
- **Custom checked exception** — `InsufficientBalanceException` forces the calling code to explicitly handle overdraft attempts instead of failing silently.
- **Real DB transactions for transfers** — `conn.setAutoCommit(false)` + `commit()`/`rollback()` ensures both legs of a transfer succeed together or not at all.

## Project Structure

```
bank-account-management-system/
├── sql/
│   └── schema.sql          -> creates the database + tables
├── src/
│   ├── Main.java            -> console menu / entry point
│   ├── BankDAO.java         -> all JDBC/SQL logic (CRUD + transactions)
│   ├── Account.java         -> model class
│   ├── DBConnection.java    -> JDBC connection setup
│   └── InsufficientBalanceException.java
└── README.md
```

## Getting Started

### 1. Set up the database
```bash
mysql -u root -p < sql/schema.sql
```
This creates the `bank_db` database with `accounts` and `transactions` tables.

### 2. Add the MySQL JDBC driver
Download `mysql-connector-j` from [dev.mysql.com/downloads/connector/j](https://dev.mysql.com/downloads/connector/j/) and add the `.jar` to your project's classpath/libraries.

### 3. Configure your credentials
`DBConnection.java` reads the DB password from an environment variable (`DB_PASSWORD`) rather than storing it in code — set this in your IDE's run configuration or your system environment before running.

### 4. Run it
Compile and run `Main.java` from your IDE, or via command line:
```bash
javac -cp .:mysql-connector-j-9.x.x.jar -d out src/*.java
java -cp out:mysql-connector-j-9.x.x.jar Main
```

## Possible Extensions

- Add authentication (admin vs. customer roles)
- Interest calculation for savings accounts
- Export transaction history to CSV/PDF
- Rebuild as a REST API with Spring Boot

---
Built by Sakhamuri Nithya Sree
