package ee.fred.coop.loanapproval.repository;

import ee.fred.coop.loanapproval.domain.entity.PaymentScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentScheduleEntryRepository extends JpaRepository<PaymentScheduleEntry, Long> {

    List<PaymentScheduleEntry> findByLoanApplicationIdOrderByPaymentNumberAsc(Long loanApplicationId);
}