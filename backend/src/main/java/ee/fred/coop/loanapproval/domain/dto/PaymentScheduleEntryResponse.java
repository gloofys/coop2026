package ee.fred.coop.loanapproval.domain.dto;

import ee.fred.coop.loanapproval.domain.entity.PaymentScheduleEntry;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentScheduleEntryResponse {

    private Long id;
    private Integer paymentNumber;
    private LocalDate paymentDate;
    private BigDecimal totalPayment;
    private BigDecimal principalPayment;
    private BigDecimal interestPayment;
    private BigDecimal remainingBalance;

    public static PaymentScheduleEntryResponse from(PaymentScheduleEntry entry) {
        PaymentScheduleEntryResponse response = new PaymentScheduleEntryResponse();
        response.id = entry.getId();
        response.paymentNumber = entry.getPaymentNumber();
        response.paymentDate = entry.getPaymentDate();
        response.totalPayment = entry.getTotalPayment();
        response.principalPayment = entry.getPrincipalPayment();
        response.interestPayment = entry.getInterestPayment();
        response.remainingBalance = entry.getRemainingBalance();
        return response;
    }

    public Long getId() {
        return id;
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