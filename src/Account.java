import java.math.BigDecimal;

/**
 * Simple model representing a row in the accounts table.
 */
public class Account {
    private int accountNo;
    private String holderName;
    private String phone;
    private BigDecimal balance;

    public Account(int accountNo, String holderName, String phone, BigDecimal balance) {
        this.accountNo = accountNo;
        this.holderName = holderName;
        this.phone = phone;
        this.balance = balance;
    }

    public int getAccountNo() { return accountNo; }
    public String getHolderName() { return holderName; }
    public String getPhone() { return phone; }
    public BigDecimal getBalance() { return balance; }

    @Override
    public String toString() {
        return String.format("Account #%d | %-20s | Phone: %-12s | Balance: Rs. %.2f",
                accountNo, holderName, phone, balance);
    }
}
