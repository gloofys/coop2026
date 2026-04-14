package ee.fred.coop.loanapproval.exception;

public class InvalidApplicationStateException extends RuntimeException {

    public InvalidApplicationStateException(String message) {
        super(message);
    }
}