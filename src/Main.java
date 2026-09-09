import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Bank Account Management System
 * Console-based menu that drives the BankDAO (JDBC + MySQL).
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final BankDAO dao = new BankDAO();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   BANK ACCOUNT MANAGEMENT SYSTEM");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");

            try {
                switch (choice) {
                    case 1 -> createAccount();
                    case 2 -> checkBalance();
                    case 3 -> deposit();
                    case 4 -> withdraw();
                    case 5 -> transfer();
                    case 6 -> transactionHistory();
                    case 7 -> listAllAccounts();
                    case 0 -> {
                        running = false;
                        System.out.println("Thank you for using the Bank Management System. Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } catch (InsufficientBalanceException e) {
                System.out.println("Transaction failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
            System.out.println();
        }

        DBConnection.closeConnection();
        sc.close();
    }

    private static void printMenu() {
        System.out.println("-----------------------------------------");
        System.out.println("1. Create Account");
        System.out.println("2. Check Balance");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer");
        System.out.println("6. Transaction History");
        System.out.println("7. List All Accounts");
        System.out.println("0. Exit");
        System.out.println("-----------------------------------------");
    }

    private static void createAccount() throws SQLException {
        System.out.print("Enter holder name: ");
        String name = sc.nextLine();
        System.out.print("Enter phone number: ");
        String phone = sc.nextLine();
        BigDecimal opening = readAmount("Enter opening balance: ");

        int accNo = dao.createAccount(name, phone, opening);
        System.out.println("Account created successfully! Account No: " + accNo);
    }

    private static void checkBalance() throws SQLException {
        int accNo = readInt("Enter account number: ");
        Account acc = dao.getAccount(accNo);
        if (acc == null) {
            System.out.println("No account found with number " + accNo);
        } else {
            System.out.println(acc);
        }
    }

    private static void deposit() throws SQLException {
        int accNo = readInt("Enter account number: ");
        BigDecimal amount = readAmount("Enter amount to deposit: ");
        BigDecimal newBalance = dao.deposit(accNo, amount);
        System.out.println("Deposit successful. New balance: Rs. " + newBalance);
    }

    private static void withdraw() throws SQLException, InsufficientBalanceException {
        int accNo = readInt("Enter account number: ");
        BigDecimal amount = readAmount("Enter amount to withdraw: ");
        BigDecimal newBalance = dao.withdraw(accNo, amount);
        System.out.println("Withdrawal successful. New balance: Rs. " + newBalance);
    }

    private static void transfer() throws SQLException, InsufficientBalanceException {
        int fromAcc = readInt("Enter your account number: ");
        int toAcc = readInt("Enter recipient account number: ");
        BigDecimal amount = readAmount("Enter amount to transfer: ");
        dao.transfer(fromAcc, toAcc, amount);
        System.out.println("Transfer successful!");
    }

    private static void transactionHistory() throws SQLException {
        int accNo = readInt("Enter account number: ");
        List<String> history = dao.getTransactionHistory(accNo);
        if (history.isEmpty()) {
            System.out.println("No transactions found for this account.");
        } else {
            System.out.println("--- Transaction History for Account #" + accNo + " ---");
            for (String line : history) {
                System.out.println(line);
            }
        }
    }

    private static void listAllAccounts() throws SQLException {
        List<Account> accounts = dao.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts in the system yet.");
        } else {
            System.out.println("--- All Accounts ---");
            for (Account acc : accounts) {
                System.out.println(acc);
            }
        }
    }

    // ---------- input helpers ----------
    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            sc.next();
        }
        int value = sc.nextInt();
        sc.nextLine(); // consume leftover newline
        return value;
    }

    private static BigDecimal readAmount(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextBigDecimal()) {
            System.out.print("Please enter a valid amount: ");
            sc.next();
        }
        BigDecimal value = sc.nextBigDecimal();
        sc.nextLine();
        return value;
    }
}
