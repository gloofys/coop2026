package ee.fred.coop.loanapproval.exception;

public class DuplicateActiveApplicationException extends RuntimeException {

    public DuplicateActiveApplicationException(String message) {
        super(message);
    }
}