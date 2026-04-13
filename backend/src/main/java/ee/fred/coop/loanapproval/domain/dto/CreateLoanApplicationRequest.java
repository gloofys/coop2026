package ee.fred.coop.loanapproval.domain.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateLoanApplicationRequest {

    @NotBlank
    @Size(max = 32)
    private String firstName;

    @NotBlank
    @Size(max = 32)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "Personal code must contain exactly 11 digits")
    private String personalCode;

    @NotNull
    @Min(6)
    @Max(360)
    private Integer loanPeriodMonths;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal interestMargin;

    @NotNull
    private BigDecimal baseInterestRate;

    @NotNull
    @DecimalMin(value = "5000.0", inclusive = true)
    private BigDecimal loanAmount;

    public CreateLoanApplicationRequest() {
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

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPersonalCode(String personalCode) {
        this.personalCode = personalCode;
    }

    public void setLoanPeriodMonths(Integer loanPeriodMonths) {
        this.loanPeriodMonths = loanPeriodMonths;
    }

    public void setInterestMargin(BigDecimal interestMargin) {
        this.interestMargin = interestMargin;
    }

    public void setBaseInterestRate(BigDecimal baseInterestRate) {
        this.baseInterestRate = baseInterestRate;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }
}