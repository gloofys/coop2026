package ee.fred.coop.loanapproval.controller;

import ee.fred.coop.loanapproval.domain.dto.CreateLoanApplicationRequest;
import ee.fred.coop.loanapproval.domain.dto.LoanApplicationDetailsResponse;
import ee.fred.coop.loanapproval.domain.dto.LoanApplicationResponse;
import ee.fred.coop.loanapproval.domain.dto.PaymentScheduleEntryResponse;
import ee.fred.coop.loanapproval.service.LoanApplicationService;
import ee.fred.coop.loanapproval.service.PaymentScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final PaymentScheduleService paymentScheduleService;

    public LoanApplicationController(
            LoanApplicationService loanApplicationService,
            PaymentScheduleService paymentScheduleService
    ) {
        this.loanApplicationService = loanApplicationService;
        this.paymentScheduleService = paymentScheduleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanApplicationResponse createApplication(@Valid @RequestBody CreateLoanApplicationRequest request) {
        return loanApplicationService.createApplication(request);
    }

    @GetMapping("/{id}")
    public LoanApplicationDetailsResponse getApplicationDetails(@PathVariable Long id) {
        return loanApplicationService.getApplicationDetails(id);
    }

    @PostMapping("/{id}/schedule")
    @ResponseStatus(HttpStatus.CREATED)
    public List<PaymentScheduleEntryResponse> generateSchedule(@PathVariable Long id) {
        return paymentScheduleService.generateSchedule(id);
    }
}