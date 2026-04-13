package ee.fred.coop.loanapproval.service;

import ee.fred.coop.loanapproval.domain.dto.CreateLoanApplicationRequest;
import ee.fred.coop.loanapproval.domain.dto.LoanApplicationResponse;
import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.domain.enums.RejectionReason;
import ee.fred.coop.loanapproval.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;

    public LoanApplicationService(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    @Transactional
    public LoanApplicationResponse createApplication(CreateLoanApplicationRequest request) {
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

        LoanApplication savedApplication = loanApplicationRepository.save(application);
        return LoanApplicationResponse.from(savedApplication);
    }
}