package ee.fred.coop.loanapproval.repository;

import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    boolean existsByPersonalCodeAndStatusIn(String personalCode, Collection<ApplicationStatus> statuses);
}