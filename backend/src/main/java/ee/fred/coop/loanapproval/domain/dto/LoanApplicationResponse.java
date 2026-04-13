package ee.fred.coop.loanapproval.domain.dto;

import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.domain.enums.RejectionReason;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanApplicationResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String personalCode;
    private Integer loanPeriodMonths;
    private BigDecimal interestMargin;
    private BigDecimal baseInterestRate;
    private BigDecimal loanAmount;
    private ApplicationStatus status;
    private RejectionReason rejectionReason;
    private LocalDateTime createdAt;

    public LoanApplicationResponse() {
    }

    public static LoanApplicationResponse from(LoanApplication application) {
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.id = application.getId();
        response.firstName = application.getFirstName();
        response.lastName = application.getLastName();
        response.personalCode = application.getPersonalCode();
        response.loanPeriodMonths = application.getLoanPeriodMonths();
        response.interestMargin = application.getInterestMargin();
        response.baseInterestRate = application.getBaseInterestRate();
        response.loanAmount = application.getLoanAmount();
        response.status = application.getStatus();
        response.rejectionReason = application.getRejectionReason();
        response.createdAt = application.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    public Integer getLoanPeriodMonths() {
        return loanPeriodMonths;
    }

    public BigDecimal getInterestMargin() {
        return interestMargin;
    }

    public BigDecimal getBaseInterestRate() {
        return baseInterestRate;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}