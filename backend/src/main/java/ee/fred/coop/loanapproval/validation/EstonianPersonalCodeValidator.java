package ee.fred.coop.loanapproval.validation;

import ee.fred.coop.loanapproval.exception.InvalidPersonalCodeException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;

@Component
public class EstonianPersonalCodeValidator {

    private static final int PERSONAL_CODE_LENGTH = 11;

    private static final int[] FIRST_STAGE_WEIGHTS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1};
    private static final int[] SECOND_STAGE_WEIGHTS = {3, 4, 5, 6, 7, 8, 9, 1, 2, 3};

    private final Clock clock;

    public EstonianPersonalCodeValidator() {
        this.clock = Clock.systemDefaultZone();
    }

    public EstonianPersonalCodeValidator(Clock clock) {
        this.clock = clock;
    }

    public boolean isValid(String personalCode) {
        if (personalCode == null || !personalCode.matches("\\d{11}")) {
            return false;
        }

        if (!isValidFirstDigit(personalCode.charAt(0))) {
            return false;
        }

        try {
            parseBirthDate(personalCode);
        } catch (InvalidPersonalCodeException exception) {
            return false;
        }

        int expectedCheckDigit = calculateCheckDigit(personalCode);
        int actualCheckDigit = Character.getNumericValue(personalCode.charAt(10));

        return expectedCheckDigit == actualCheckDigit;
    }

    public void validate(String personalCode) {
        if (!isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid Estonian personal code");
        }
    }

    public LocalDate getBirthDate(String personalCode) {
        validate(personalCode);
        return parseBirthDate(personalCode);
    }

    public int getAge(String personalCode) {
        LocalDate birthDate = getBirthDate(personalCode);
        return Period.between(birthDate, LocalDate.now(clock)).getYears();
    }

    private boolean isValidFirstDigit(char firstDigitChar) {
        int firstDigit = Character.getNumericValue(firstDigitChar);
        return firstDigit >= 1 && firstDigit <= 8;
    }

    private LocalDate parseBirthDate(String personalCode) {
        int firstDigit = Character.getNumericValue(personalCode.charAt(0));
        int yearPart = Integer.parseInt(personalCode.substring(1, 3));
        int month = Integer.parseInt(personalCode.substring(3, 5));
        int day = Integer.parseInt(personalCode.substring(5, 7));

        int century = resolveCentury(firstDigit);
        int fullYear = century + yearPart;

        try {
            return LocalDate.of(fullYear, month, day);
        } catch (DateTimeException exception) {
            throw new InvalidPersonalCodeException("Invalid birth date in personal code");
        }
    }

    private int resolveCentury(int firstDigit) {
        return switch (firstDigit) {
            case 1, 2 -> 1800;
            case 3, 4 -> 1900;
            case 5, 6 -> 2000;
            case 7, 8 -> 2100;
            default -> throw new InvalidPersonalCodeException("Invalid first digit in personal code");
        };
    }

    private int calculateCheckDigit(String personalCode) {
        int sum = weightedSum(personalCode, FIRST_STAGE_WEIGHTS);
        int remainder = sum % 11;

        if (remainder < 10) {
            return remainder;
        }

        sum = weightedSum(personalCode, SECOND_STAGE_WEIGHTS);
        remainder = sum % 11;

        if (remainder < 10) {
            return remainder;
        }

        return 0;
    }

    private int weightedSum(String personalCode, int[] weights) {
        int sum = 0;

        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(personalCode.charAt(i));
            sum += digit * weights[i];
        }

        return sum;
    }
}