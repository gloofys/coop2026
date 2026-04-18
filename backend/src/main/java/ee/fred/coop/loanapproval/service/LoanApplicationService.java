package ee.fred.coop.loanapproval.service;

import ee.fred.coop.loanapproval.domain.dto.CreateLoanApplicationRequest;
import ee.fred.coop.loanapproval.domain.dto.LoanApplicationDetailsResponse;
import ee.fred.coop.loanapproval.domain.dto.LoanApplicationResponse;
import ee.fred.coop.loanapproval.domain.dto.RejectLoanApplicationRequest;
import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.entity.PaymentScheduleEntry;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.domain.enums.RejectionReason;
import ee.fred.coop.loanapproval.exception.DuplicateActiveApplicationException;
import ee.fred.coop.loanapproval.exception.InvalidApplicationStateException;
import ee.fred.coop.loanapproval.exception.LoanApplicationNotFoundException;
import ee.fred.coop.loanapproval.repository.LoanApplicationRepository;
import ee.fred.coop.loanapproval.repository.PaymentScheduleEntryRepository;
import ee.fred.coop.loanapproval.validation.EstonianPersonalCodeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final PaymentScheduleEntryRepository paymentScheduleEntryRepository;
    private final EstonianPersonalCodeValidator personalCodeValidator;
    private final int maxCustomerAge;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            PaymentScheduleEntryRepository paymentScheduleEntryRepository,
            EstonianPersonalCodeValidator personalCodeValidator,
            @Value("${app.loan.max-customer-age}") int maxCustomerAge
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.paymentScheduleEntryRepository = paymentScheduleEntryRepository;
        this.personalCodeValidator = personalCodeValidator;
        this.maxCustomerAge = maxCustomerAge;
    }

    @Transactional
    public LoanApplicationResponse createApplication(CreateLoanApplicationRequest request) {
        personalCodeValidator.validate(request.getPersonalCode());
        int age = personalCodeValidator.getAge(request.getPersonalCode());

        if (age > maxCustomerAge) {
            LoanApplication rejectedApplication = new LoanApplication(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPersonalCode(),
                    request.getLoanPeriodMonths(),
                    request.getInterestMargin(),
                    request.getBaseInterestRate(),
                    request.getLoanAmount(),
                    ApplicationStatus.REJECTED,
                    RejectionReason.CUSTOMER_TOO_OLD
            );

            LoanApplication savedApplication = loanApplicationRepository.save(rejectedApplication);
            return LoanApplicationResponse.from(savedApplication);
        }

        boolean hasActiveApplication = loanApplicationRepository.existsByPersonalCodeAndStatusIn(
                request.getPersonalCode(),
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.IN_REVIEW)
        );

        if (hasActiveApplication) {
            throw new DuplicateActiveApplicationException(
                    "Customer already has an active loan application"
            );
        }

        LoanApplication application = new LoanApplication(
                request.getFirstName(),
                request.getLastName(),
                request.getPersonalCode(),
                request.getLoanPeriodMonths(),
                request.getInterestMargin(),
                request.getBaseInterestRate(),
                request.getLoanAmount(),
                ApplicationStatus.SUBMITTED,
                null
        );

        LoanApplication savedApplication;
        try {
            savedApplication = loanApplicationRepository.save(application);
        } catch (DataIntegrityViolationException exception) {
            if (isActiveApplicationConflict(exception)) {
                throw new DuplicateActiveApplicationException(
                        "Customer already has an active loan application"
                );
            }

            throw exception;
        }

        return LoanApplicationResponse.from(savedApplication);
    }

    @Transactional(readOnly = true)
    public LoanApplicationDetailsResponse getApplicationDetails(Long loanApplicationId) {
        LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException(
                        "Loan application not found with id " + loanApplicationId
                ));

        List<PaymentScheduleEntry> scheduleEntries =
                paymentScheduleEntryRepository.findByLoanApplicationIdOrderByPaymentNumberAsc(loanApplicationId);

        return LoanApplicationDetailsResponse.from(application, scheduleEntries);
    }
    @Transactional
    public LoanApplicationResponse approveApplication(Long loanApplicationId) {
        LoanApplication application = loanApplicationRepository.findByIdForUpdate(loanApplicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException(
                        "Loan application not found with id " + loanApplicationId
                ));

        if (application.getStatus() != ApplicationStatus.IN_REVIEW) {
            throw new InvalidApplicationStateException(
                    "Only IN_REVIEW applications can be approved"
            );
        }

        application.setStatus(ApplicationStatus.APPROVED);
        application.setRejectionReason(null);

        LoanApplication savedApplication = loanApplicationRepository.save(application);
        return LoanApplicationResponse.from(savedApplication);
    }

    @Transactional
    public LoanApplicationResponse rejectApplication(
            Long loanApplicationId,
            RejectLoanApplicationRequest request
    ) {
        LoanApplication application = loanApplicationRepository.findByIdForUpdate(loanApplicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException(
                        "Loan application not found with id " + loanApplicationId
                ));

        if (application.getStatus() != ApplicationStatus.IN_REVIEW) {
            throw new InvalidApplicationStateException(
                    "Only IN_REVIEW applications can be rejected"
            );
        }

        application.setStatus(ApplicationStatus.REJECTED);
        application.setRejectionReason(request.getReason());

        LoanApplication savedApplication = loanApplicationRepository.save(application);
        return LoanApplicationResponse.from(savedApplication);
    }

    private boolean isActiveApplicationConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.contains("uq_loan_application_active_personal_code")) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }
}
