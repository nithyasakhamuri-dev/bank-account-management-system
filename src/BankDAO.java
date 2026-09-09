import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object - every database operation for the bank system
 * lives here, keeping SQL out of Main.java.
 */
public class BankDAO {

    // ---------- CREATE ACCOUNT ----------
    public int createAccount(String holderName, String phone, BigDecimal openingBalance) throws SQLException {
        String sql = "INSERT INTO accounts (holder_name, phone, balance) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, holderName);
            ps.setString(2, phone);
            ps.setBigDecimal(3, openingBalance);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newAccountNo = keys.getInt(1);
                    if (openingBalance.compareTo(BigDecimal.ZERO) > 0) {
                        logTransaction(newAccountNo, "DEPOSIT", openingBalance, openingBalance, "Opening balance");
                    }
                    return newAccountNo;
                }
            }
        }
        throw new SQLException("Account creation failed, no ID obtained.");
    }

    // ---------- READ ----------
    public Account getAccount(int accountNo) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_no = ?";
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("account_no"),
                            rs.getString("holder_name"),
                            rs.getString("phone"),
                            rs.getBigDecimal("balance"));
                }
            }
        }
        return null; // not found
    }

    public List<Account> getAllAccounts() throws SQLException {
        String sql = "SELECT * FROM accounts ORDER BY account_no";
        List<Account> accounts = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                accounts.add(new Account(
                        rs.getInt("account_no"),
                        rs.getString("holder_name"),
                        rs.getString("phone"),
                        rs.getBigDecimal("balance")));
            }
        }
        return accounts;
    }

    // ---------- DEPOSIT ----------
    public BigDecimal deposit(int accountNo, BigDecimal amount) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        Account acc = requireAccount(accountNo);
        BigDecimal newBalance = acc.getBalance().add(amount);

        updateBalance(accountNo, newBalance);
        logTransaction(accountNo, "DEPOSIT", amount, newBalance, "Cash deposit");
        return newBalance;
    }

    // ---------- WITHDRAW ----------
    public BigDecimal withdraw(int accountNo, BigDecimal amount) throws SQLException, InsufficientBalanceException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        Account acc = requireAccount(accountNo);
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: Rs. " + acc.getBalance());
        }
        BigDecimal newBalance = acc.getBalance().subtract(amount);

        updateBalance(accountNo, newBalance);
        logTransaction(accountNo, "WITHDRAW", amount, newBalance, "Cash withdrawal");
        return newBalance;
    }

    // ---------- TRANSFER (uses a DB transaction: both legs succeed or neither does) ----------
    public void transfer(int fromAccountNo, int toAccountNo, BigDecimal amount)
            throws SQLException, InsufficientBalanceException {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        if (fromAccountNo == toAccountNo) {
            throw new IllegalArgumentException("Cannot transfer to the same account.");
        }

        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            Account from = requireAccount(fromAccountNo);
            Account to = requireAccount(toAccountNo);

            if (from.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance in account #" + fromAccountNo);
            }

            BigDecimal fromNewBalance = from.getBalance().subtract(amount);
            BigDecimal toNewBalance = to.getBalance().add(amount);

            updateBalance(fromAccountNo, fromNewBalance);
            updateBalance(toAccountNo, toNewBalance);

            logTransaction(fromAccountNo, "TRANSFER_OUT", amount, fromNewBalance, "Transfer to #" + toAccountNo);
            logTransaction(toAccountNo, "TRANSFER_IN", amount, toNewBalance, "Transfer from #" + fromAccountNo);

            conn.commit();
        } catch (SQLException | InsufficientBalanceException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ---------- TRANSACTION HISTORY ----------
    public List<String> getTransactionHistory(int accountNo) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_no = ? ORDER BY txn_time DESC";
        List<String> history = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp time = rs.getTimestamp("txn_time");
                    history.add(String.format("[%s] %-13s Rs. %10.2f | Balance after: Rs. %10.2f | %s",
                            time, rs.getString("txn_type"), rs.getBigDecimal("amount"),
                            rs.getBigDecimal("balance_after"), rs.getString("remarks")));
                }
            }
        }
        return history;
    }

    // ---------- helpers ----------
    private Account requireAccount(int accountNo) throws SQLException {
        Account acc = getAccount(accountNo);
        if (acc == null) {
            throw new IllegalArgumentException("No account found with number " + accountNo);
        }
        return acc;
    }

    private void updateBalance(int accountNo, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_no = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountNo);
            ps.executeUpdate();
        }
    }

    private void logTransaction(int accountNo, String type, BigDecimal amount,
                                 BigDecimal balanceAfter, String remarks) throws SQLException {
        String sql = "INSERT INTO transactions (account_no, txn_type, amount, balance_after, remarks) "
                + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNo);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            ps.setBigDecimal(4, balanceAfter);
            ps.setString(5, remarks);
            ps.executeUpdate();
        }
    }
}
