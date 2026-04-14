package ee.fred.coop.loanapproval.service;

import ee.fred.coop.loanapproval.domain.dto.PaymentScheduleEntryResponse;
import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.entity.PaymentScheduleEntry;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.exception.InvalidApplicationStateException;
import ee.fred.coop.loanapproval.exception.LoanApplicationNotFoundException;
import ee.fred.coop.loanapproval.repository.LoanApplicationRepository;
import ee.fred.coop.loanapproval.repository.PaymentScheduleEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentScheduleService {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final LoanApplicationRepository loanApplicationRepository;
    private final PaymentScheduleEntryRepository paymentScheduleEntryRepository;

    public PaymentScheduleService(
            LoanApplicationRepository loanApplicationRepository,
            PaymentScheduleEntryRepository paymentScheduleEntryRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.paymentScheduleEntryRepository = paymentScheduleEntryRepository;
    }

    @Transactional
    public List<PaymentScheduleEntryResponse> generateSchedule(Long loanApplicationId) {
        LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException(
                        "Loan application not found with id " + loanApplicationId
                ));

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new InvalidApplicationStateException(
                    "Payment schedule can only be generated for SUBMITTED applications"
            );
        }

        List<PaymentScheduleEntry> entries = buildScheduleEntries(application);
        List<PaymentScheduleEntry> savedEntries = paymentScheduleEntryRepository.saveAll(entries);

        application.setStatus(ApplicationStatus.IN_REVIEW);

        return savedEntries.stream()
                .map(PaymentScheduleEntryResponse::from)
                .toList();
    }

    private List<PaymentScheduleEntry> buildScheduleEntries(LoanApplication application) {
        BigDecimal principal = application.getLoanAmount();
        int months = application.getLoanPeriodMonths();
        BigDecimal annualInterestRate = application.getInterestMargin()
                .add(application.getBaseInterestRate());

        BigDecimal monthlyPayment = calculateMonthlyPayment(principal, annualInterestRate, months);
        BigDecimal remainingBalance = principal;
        LocalDate firstPaymentDate = LocalDate.now();

        List<PaymentScheduleEntry> entries = new ArrayList<>();

        for (int paymentNumber = 1; paymentNumber <= months; paymentNumber++) {
            LocalDate paymentDate = firstPaymentDate.plusMonths(paymentNumber - 1);

            BigDecimal interestPayment = calculateMonthlyInterest(remainingBalance, annualInterestRate);

            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment, MATH_CONTEXT)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            if (paymentNumber == months) {
                principalPayment = remainingBalance.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
                monthlyPayment = principalPayment.add(interestPayment).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            }

            remainingBalance = remainingBalance.subtract(principalPayment, MATH_CONTEXT)
                    .max(BigDecimal.ZERO)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            PaymentScheduleEntry entry = new PaymentScheduleEntry(
                    application,
                    paymentNumber,
                    paymentDate,
                    monthlyPayment.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                    principalPayment,
                    interestPayment,
                    remainingBalance
            );

            entries.add(entry);
        }

        return entries;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualInterestRate, int months) {
        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualInterestRate
                .divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);

        double r = monthlyRate.doubleValue();
        double denominator = 1 - Math.pow(1 + r, -months);
        double payment = principal.doubleValue() * r / denominator;

        return BigDecimal.valueOf(payment).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlyInterest(BigDecimal remainingBalance, BigDecimal annualInterestRate) {
        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualInterestRate
                .divide(ONE_HUNDRED, 10, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);

        return remainingBalance.multiply(monthlyRate, MATH_CONTEXT)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}