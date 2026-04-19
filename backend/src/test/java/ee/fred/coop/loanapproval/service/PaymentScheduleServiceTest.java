package ee.fred.coop.loanapproval.service;

import ee.fred.coop.loanapproval.domain.dto.PaymentScheduleEntryResponse;
import ee.fred.coop.loanapproval.domain.entity.LoanApplication;
import ee.fred.coop.loanapproval.domain.entity.PaymentScheduleEntry;
import ee.fred.coop.loanapproval.domain.enums.ApplicationStatus;
import ee.fred.coop.loanapproval.exception.InvalidApplicationStateException;
import ee.fred.coop.loanapproval.exception.LoanApplicationNotFoundException;
import ee.fred.coop.loanapproval.repository.LoanApplicationRepository;
import ee.fred.coop.loanapproval.repository.PaymentScheduleEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentScheduleServiceTest {

    private LoanApplicationRepository loanApplicationRepository;
    private PaymentScheduleEntryRepository paymentScheduleEntryRepository;
    private PaymentScheduleService paymentScheduleService;

    @BeforeEach
    void setUp() {
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        paymentScheduleEntryRepository = mock(PaymentScheduleEntryRepository.class);

        paymentScheduleService = new PaymentScheduleService(
                loanApplicationRepository,
                paymentScheduleEntryRepository
        );
    }

    @Test
    void generateSchedule_shouldGenerateEntriesAndMoveApplicationToInReview_whenApplicationIsSubmitted() {
        LoanApplication application = buildApplication(ApplicationStatus.SUBMITTED);

        when(loanApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(application));
        when(paymentScheduleEntryRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<PaymentScheduleEntry> entries = invocation.getArgument(0);
                    for (int i = 0; i < entries.size(); i++) {
                        setPrivateField(entries.get(i), "id", (long) (i + 1));
                    }
                    return entries;
                });

        List<PaymentScheduleEntryResponse> result = paymentScheduleService.generateSchedule(1L);

        assertEquals(6, result.size());
        assertEquals(ApplicationStatus.IN_REVIEW, application.getStatus());

        PaymentScheduleEntryResponse firstEntry = result.get(0);
        assertEquals(1, firstEntry.getPaymentNumber());
        assertNotNull(firstEntry.getPaymentDate());
        assertNotNull(firstEntry.getTotalPayment());
        assertNotNull(firstEntry.getPrincipalPayment());
        assertNotNull(firstEntry.getInterestPayment());
        assertNotNull(firstEntry.getRemainingBalance());

        verify(loanApplicationRepository).findByIdForUpdate(1L);
        verify(paymentScheduleEntryRepository).saveAll(anyList());
    }

    @Test
    void generateSchedule_shouldThrowException_whenApplicationIsNotFound() {
        when(loanApplicationRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(
                LoanApplicationNotFoundException.class,
                () -> paymentScheduleService.generateSchedule(99L)
        );

        verify(paymentScheduleEntryRepository, never()).saveAll(anyList());
    }

    @Test
    void generateSchedule_shouldThrowException_whenApplicationIsNotSubmitted() {
        LoanApplication application = buildApplication(ApplicationStatus.IN_REVIEW);

        when(loanApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(application));

        assertThrows(
                InvalidApplicationStateException.class,
                () -> paymentScheduleService.generateSchedule(1L)
        );

        verify(paymentScheduleEntryRepository, never()).saveAll(anyList());
    }

    @Test
    void generateSchedule_shouldCreateZeroInterestSchedule_whenInterestRateIsZero() {
        LoanApplication application = buildZeroInterestApplication(ApplicationStatus.SUBMITTED);

        when(loanApplicationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(application));
        when(paymentScheduleEntryRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<PaymentScheduleEntryResponse> result = paymentScheduleService.generateSchedule(1L);

        assertEquals(6, result.size());
        assertEquals(new BigDecimal("833.33"), result.get(0).getTotalPayment());
        assertEquals(new BigDecimal("0.00"), result.get(0).getInterestPayment());
        assertEquals(ApplicationStatus.IN_REVIEW, application.getStatus());
    }

    private LoanApplication buildApplication(ApplicationStatus status) {
        LoanApplication application = new LoanApplication(
                "Fred",
                "Brosman",
                "39107260265",
                6,
                new BigDecimal("2.500"),
                new BigDecimal("1.234"),
                new BigDecimal("5000.00"),
                status,
                null
        );

        setPrivateField(application, "id", 1L);
        setPrivateField(application, "createdAt", LocalDateTime.now());

        return application;
    }

    private LoanApplication buildZeroInterestApplication(ApplicationStatus status) {
        LoanApplication application = new LoanApplication(
                "Fred",
                "Brosman",
                "39107260265",
                6,
                new BigDecimal("0.000"),
                new BigDecimal("0.000"),
                new BigDecimal("5000.00"),
                status,
                null
        );

        setPrivateField(application, "id", 1L);
        setPrivateField(application, "createdAt", LocalDateTime.now());

        return application;
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
