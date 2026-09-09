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
---
Built by Sakhamuri Nithya Sree
