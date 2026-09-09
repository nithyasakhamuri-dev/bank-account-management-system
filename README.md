# Bank Account Management System

A console-based Bank Account Management System built with **Java (JDBC)** and **MySQL**.

## Features
- Create a new account (with opening balance)
- Check balance
- Deposit money
- Withdraw money (blocks overdrafts via `InsufficientBalanceException`)
- Transfer money between accounts (uses a real DB transaction — commit/rollback, so either both balances update or neither does)
- View transaction history per account
- List all accounts

## Project Structure
```
BankManagementSystem/
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

## Setup

### 1. Create the database
Make sure MySQL is running locally, then:
```bash
mysql -u root -p < sql/schema.sql
```
This creates the `bank_db` database with `accounts` and `transactions` tables.

### 2. Get the MySQL JDBC driver
Download `mysql-connector-j` (the MySQL Connector/J `.jar`) from:
https://dev.mysql.com/downloads/connector/j/

### 3. Update your DB credentials
In `src/DBConnection.java`, change:
```java
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_mysql_password";
```
to match your local MySQL setup.

### 4. Compile and run

**If using plain javac (command line):**
```bash
cd src
javac -cp .:/path/to/mysql-connector-j-8.x.x.jar -d ../out *.java
java -cp ../out:/path/to/mysql-connector-j-8.x.x.jar Main
```
(On Windows, use `;` instead of `:` in the classpath.)

**If using an IDE (IntelliJ / Eclipse):**
1. Create a new Java project, add all files from `src/` into it.
2. Add the `mysql-connector-j` jar to the project's libraries/dependencies.
3. Run `Main.java`.

## How it works (for your resume talking points)
- **DAO pattern**: `BankDAO` separates all SQL from the console/menu logic in `Main`.
- **PreparedStatement everywhere**: prevents SQL injection, all user input is parameterized.
- **BigDecimal for money**: avoids floating-point rounding errors that `double`/`float` would cause with currency.
- **Real DB transactions for transfers**: `conn.setAutoCommit(false)` + `commit()`/`rollback()` ensures a transfer can never leave money "stuck" between accounts if something fails mid-way.
- **Custom checked exception** (`InsufficientBalanceException`): forces callers to explicitly handle the overdraft case instead of silently failing.
- **Every transaction is logged**: the `transactions` table gives a full audit trail per account.

## Possible extensions (good to mention as "future work")
- Add login/authentication (admin vs customer roles)
- Add interest calculation on savings accounts
- Export transaction history to PDF/CSV
- Wrap this in a Spring Boot REST API once you're comfortable with Spring Boot
