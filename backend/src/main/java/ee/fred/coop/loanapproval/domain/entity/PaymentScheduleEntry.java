package ee.fred.coop.loanapproval.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_schedule_entry")
public class PaymentScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(name = "payment_number", nullable = false)
    private Integer paymentNumber;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "total_payment", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalPayment;

    @Column(name = "principal_payment", precision = 12, scale = 2, nullable = false)
    private BigDecimal principalPayment;

    @Column(name = "interest_payment", precision = 12, scale = 2, nullable = false)
    private BigDecimal interestPayment;

    @Column(name = "remaining_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal remainingBalance;

    protected PaymentScheduleEntry() {
    }

    public PaymentScheduleEntry(
            LoanApplication loanApplication,
            Integer paymentNumber,
            LocalDate paymentDate,
            BigDecimal totalPayment,
            BigDecimal principalPayment,
            BigDecimal interestPayment,
            BigDecimal remainingBalance
    ) {
        this.loanApplication = loanApplication;
        this.paymentNumber = paymentNumber;
        this.paymentDate = paymentDate;
        this.totalPayment = totalPayment;
        this.principalPayment = principalPayment;
        this.interestPayment = interestPayment;
        this.remainingBalance = remainingBalance;
    }

    public Long getId() {
        return id;
    }

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public Integer getPaymentNumber() {
        return paymentNumber;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public BigDecimal getPrincipalPayment() {
        return principalPayment;
    }

    public BigDecimal getInterestPayment() {
        return interestPayment;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }
}