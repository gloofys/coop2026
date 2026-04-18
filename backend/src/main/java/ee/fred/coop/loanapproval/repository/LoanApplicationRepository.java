package ee.fred.coop.loanapproval.repository;

import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    boolean existsByPersonalCodeAndStatusIn(String personalCode, Collection<ApplicationStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select loanApplication from LoanApplication loanApplication where loanApplication.id = :id")
    Optional<LoanApplication> findByIdForUpdate(@Param("id") Long id);
}
