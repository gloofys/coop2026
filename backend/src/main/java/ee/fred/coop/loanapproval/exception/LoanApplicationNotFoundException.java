package ee.fred.coop.loanapproval.exception;

public class LoanApplicationNotFoundException extends RuntimeException {

    public LoanApplicationNotFoundException(String message) {
        super(message);
    }
}