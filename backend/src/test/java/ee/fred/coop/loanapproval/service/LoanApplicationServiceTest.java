package ee.fred.coop.loanapproval.service;

import ee.fred.coop.loanapproval.domain.dto.CreateLoanApplicationRequest;
import ee.fred.coop.loanapproval.domain.dto.LoanApplicationResponse;
import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.domain.enums.RejectionReason;
import ee.fred.coop.loanapproval.exception.DuplicateActiveApplicationException;
import ee.fred.coop.loanapproval.exception.InvalidPersonalCodeException;
import ee.fred.coop.loanapproval.repository.LoanApplicationRepository;
import ee.fred.coop.loanapproval.repository.PaymentScheduleEntryRepository;
import ee.fred.coop.loanapproval.validation.EstonianPersonalCodeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    private LoanApplicationRepository loanApplicationRepository;
    private PaymentScheduleEntryRepository paymentScheduleEntryRepository;
    private EstonianPersonalCodeValidator personalCodeValidator;
    private LoanApplicationService loanApplicationService;

    @BeforeEach
    void setUp() {
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        paymentScheduleEntryRepository = mock(PaymentScheduleEntryRepository.class);
        personalCodeValidator = mock(EstonianPersonalCodeValidator.class);

        loanApplicationService = new LoanApplicationService(
                loanApplicationRepository,
                paymentScheduleEntryRepository,
                personalCodeValidator,
                70
        );
    }

    @Test
    void createApplication_shouldSaveSubmittedApplication_whenApplicantIsValidAndUnderAgeLimit() {
        CreateLoanApplicationRequest request = buildRequest();

        doNothing().when(personalCodeValidator).validate(request.getPersonalCode());
        when(loanApplicationRepository.existsByPersonalCodeAndStatusIn(anyString(), anyCollection()))
                .thenReturn(false);
        when(personalCodeValidator.getAge(request.getPersonalCode())).thenReturn(30);

        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> {
                    LoanApplication application = invocation.getArgument(0);
                    setPrivateField(application, "id", 1L);
                    setPrivateField(application, "createdAt", LocalDateTime.now());
                    return application;
                });

        LoanApplicationResponse response = loanApplicationService.createApplication(request);

        assertEquals(ApplicationStatus.SUBMITTED, response.getStatus());
        assertNull(response.getRejectionReason());
        assertEquals(request.getPersonalCode(), response.getPersonalCode());

        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(captor.capture());

        LoanApplication savedApplication = captor.getValue();
        assertEquals(ApplicationStatus.SUBMITTED, savedApplication.getStatus());
        assertNull(savedApplication.getRejectionReason());
    }

    @Test
    void createApplication_shouldSaveRejectedApplication_whenApplicantIsTooOld() {
        CreateLoanApplicationRequest request = buildRequest();

        doNothing().when(personalCodeValidator).validate(request.getPersonalCode());
        when(loanApplicationRepository.existsByPersonalCodeAndStatusIn(anyString(), anyCollection()))
                .thenReturn(false);
        when(personalCodeValidator.getAge(request.getPersonalCode())).thenReturn(75);

        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> {
                    LoanApplication application = invocation.getArgument(0);
                    setPrivateField(application, "id", 2L);
                    setPrivateField(application, "createdAt", LocalDateTime.now());
                    return application;
                });

        LoanApplicationResponse response = loanApplicationService.createApplication(request);

        assertEquals(ApplicationStatus.REJECTED, response.getStatus());
        assertEquals(RejectionReason.CUSTOMER_TOO_OLD, response.getRejectionReason());

        ArgumentCaptor<LoanApplication> captor = ArgumentCaptor.forClass(LoanApplication.class);
        verify(loanApplicationRepository).save(captor.capture());

        LoanApplication savedApplication = captor.getValue();
        assertEquals(ApplicationStatus.REJECTED, savedApplication.getStatus());
        assertEquals(RejectionReason.CUSTOMER_TOO_OLD, savedApplication.getRejectionReason());
    }

    @Test
    void createApplication_shouldThrowException_whenCustomerAlreadyHasActiveApplication() {
        CreateLoanApplicationRequest request = buildRequest();

        doNothing().when(personalCodeValidator).validate(request.getPersonalCode());
        when(loanApplicationRepository.existsByPersonalCodeAndStatusIn(anyString(), anyCollection()))
                .thenReturn(true);

        assertThrows(
                DuplicateActiveApplicationException.class,
                () -> loanApplicationService.createApplication(request)
        );

        verify(loanApplicationRepository, never()).save(any());
        verify(personalCodeValidator, never()).getAge(anyString());
    }

    @Test
    void createApplication_shouldThrowException_whenPersonalCodeIsInvalid() {
        CreateLoanApplicationRequest request = buildRequest();

        doThrow(new InvalidPersonalCodeException("Invalid Estonian personal code"))
                .when(personalCodeValidator)
                .validate(request.getPersonalCode());

        assertThrows(
                InvalidPersonalCodeException.class,
                () -> loanApplicationService.createApplication(request)
        );

        verify(loanApplicationRepository, never()).existsByPersonalCodeAndStatusIn(anyString(), anyCollection());
        verify(loanApplicationRepository, never()).save(any());
    }

    private CreateLoanApplicationRequest buildRequest() {
        CreateLoanApplicationRequest request = new CreateLoanApplicationRequest();
        request.setFirstName("Fred");
        request.setLastName("Brosman");
        request.setPersonalCode("39107260265");
        request.setLoanPeriodMonths(12);
        request.setInterestMargin(new BigDecimal("2.500"));
        request.setBaseInterestRate(new BigDecimal("1.234"));
        request.setLoanAmount(new BigDecimal("7000.00"));
        return request;
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}