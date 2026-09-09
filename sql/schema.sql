-- Bank Account Management System - Database Schema
-- Run this once in MySQL before starting the Java app:
--   mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

-- Stores each customer's account
CREATE TABLE IF NOT EXISTS accounts (
    account_no      INT AUTO_INCREMENT PRIMARY KEY,
    holder_name     VARCHAR(100) NOT NULL,
    phone           VARCHAR(15),
    balance         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Stores every deposit / withdrawal / transfer as a record
CREATE TABLE IF NOT EXISTS transactions (
    txn_id          INT AUTO_INCREMENT PRIMARY KEY,
    account_no      INT NOT NULL,
    txn_type        ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER_OUT', 'TRANSFER_IN') NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    balance_after   DECIMAL(15,2) NOT NULL,
    txn_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks         VARCHAR(255),
    FOREIGN KEY (account_no) REFERENCES accounts(account_no)
);
