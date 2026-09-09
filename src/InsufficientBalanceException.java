/**
 * Thrown when a withdrawal or transfer is attempted for more
 * than the account's current balance.
 */
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
