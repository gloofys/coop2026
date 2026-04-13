package ee.fred.coop.loanapproval.exception;

public class InvalidPersonalCodeException extends RuntimeException {

    public InvalidPersonalCodeException(String message) {
        super(message);
    }
}