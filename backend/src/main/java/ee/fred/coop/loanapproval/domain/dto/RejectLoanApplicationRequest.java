package ee.fred.coop.loanapproval.domain.dto;

import ee.fred.coop.loanapproval.domain.enums.RejectionReason;
import jakarta.validation.constraints.NotNull;

public class RejectLoanApplicationRequest {

    @NotNull
    private RejectionReason reason;

    public RejectLoanApplicationRequest() {
    }

    public RejectionReason getReason() {
        return reason;
    }

    public void setReason(RejectionReason reason) {
        this.reason = reason;
    }
}