package ee.fred.coop.loanapproval.domain.entity;

import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.domain.enums.RejectionReason;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_application")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 32, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 32, nullable = false)
    private String lastName;

    @Column(name = "personal_code", length = 11, nullable = false)
    private String personalCode;

    @Column(name = "loan_period_months", nullable = false)
    private Integer loanPeriodMonths;

    @Column(name = "interest_margin", precision = 10, scale = 3, nullable = false)
    private BigDecimal interestMargin;

    @Column(name = "base_interest_rate", precision = 10, scale = 3, nullable = false)
    private BigDecimal baseInterestRate;

    @Column(name = "loan_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal loanAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason", length = 64)
    private RejectionReason rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LoanApplication() {
    }

    public LoanApplication(
            String firstName,
            String lastName,
            String personalCode,
            Integer loanPeriodMonths,
            BigDecimal interestMargin,
            BigDecimal baseInterestRate,
            BigDecimal loanAmount,
            ApplicationStatus status,
            RejectionReason rejectionReason
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalCode = personalCode;
        this.loanPeriodMonths = loanPeriodMonths;
        this.interestMargin = interestMargin;
        this.baseInterestRate = baseInterestRate;
        this.loanAmount = loanAmount;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
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

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setRejectionReason(RejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public boolean isActive() {
        return status == ApplicationStatus.SUBMITTED || status == ApplicationStatus.IN_REVIEW;
    }
}