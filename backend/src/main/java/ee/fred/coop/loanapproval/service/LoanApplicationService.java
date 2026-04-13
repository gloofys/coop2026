package ee.fred.coop.loanapproval.service;

import ee.fred.coop.loanapproval.domain.dto.CreateLoanApplicationRequest;
import ee.fred.coop.loanapproval.domain.dto.LoanApplicationResponse;
import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.domain.enums.RejectionReason;
import ee.fred.coop.loanapproval.repository.LoanApplicationRepository;
import ee.fred.coop.loanapproval.validation.EstonianPersonalCodeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationService {

    private static final int MAX_CUSTOMER_AGE = 70;

    private final LoanApplicationRepository loanApplicationRepository;
    private final EstonianPersonalCodeValidator personalCodeValidator;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            EstonianPersonalCodeValidator personalCodeValidator
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.personalCodeValidator = personalCodeValidator;
    }

    @Transactional
    public LoanApplicationResponse createApplication(CreateLoanApplicationRequest request) {
        personalCodeValidator.validate(request.getPersonalCode());
        int age = personalCodeValidator.getAge(request.getPersonalCode());

        LoanApplication application;

        if (age > MAX_CUSTOMER_AGE) {
            application = new LoanApplication(
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
        } else {
            application = new LoanApplication(
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
        }

        LoanApplication savedApplication = loanApplicationRepository.save(application);
        return LoanApplicationResponse.from(savedApplication);
    }
}