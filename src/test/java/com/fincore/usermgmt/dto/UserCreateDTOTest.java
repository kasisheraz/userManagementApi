package com.fincore.usermgmt.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserCreateDTO Validation Tests")
class UserCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Valid DTO Tests ====================

    @Test
    @DisplayName("Valid UserCreateDTO with all required fields should pass validation")
    void validUserCreateDTO_withAllRequiredFields_shouldPassValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid UserCreateDTO with all fields should pass validation")
    void validUserCreateDTO_withAllFields_shouldPassValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setMiddleName("Michael");
        dto.setLastName("Doe");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setResidentialAddressIdentifier(1);
        dto.setPostalAddressIdentifier(2);
        dto.setStatusDescription("ACTIVE");
        dto.setRole("USER");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid UserCreateDTO with maximum length fields should pass validation")
    void validUserCreateDTO_withMaxLengthFields_shouldPassValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("12345678901234567890"); // 20 chars (max)
        dto.setEmail("a".repeat(40) + "@test.com"); // 50 chars (max)
        dto.setFirstName("a".repeat(100)); // 100 chars (max)
        dto.setMiddleName("b".repeat(100)); // 100 chars (max)
        dto.setLastName("c".repeat(100)); // 100 chars (max)
        dto.setStatusDescription("a".repeat(20)); // 20 chars (max)

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    // ==================== PhoneNumber Validation Tests ====================

    @Test
    @DisplayName("UserCreateDTO with null phoneNumber should fail validation")
    void userCreateDTO_withNullPhoneNumber_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber(null);
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("phoneNumber");
        assertThat(violations.iterator().next().getMessage()).contains("must not be blank");
    }

    @Test
    @DisplayName("UserCreateDTO with empty phoneNumber should fail validation")
    void userCreateDTO_withEmptyPhoneNumber_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("phoneNumber");
    }

    @Test
    @DisplayName("UserCreateDTO with blank phoneNumber should fail validation")
    void userCreateDTO_withBlankPhoneNumber_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("   ");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("phoneNumber");
    }

    @Test
    @DisplayName("UserCreateDTO with phoneNumber exceeding max length should fail validation")
    void userCreateDTO_withPhoneNumberExceedingMaxLength_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("123456789012345678901"); // 21 chars (max is 20)
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("phoneNumber");
        assertThat(violations.iterator().next().getMessage()).contains("size must be between");
    }

    // ==================== Email Validation Tests ====================

    @Test
    @DisplayName("UserCreateDTO with null email should pass validation (email is optional)")
    void userCreateDTO_withNullEmail_shouldPassValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail(null);
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserCreateDTO with invalid email format should fail validation")
    void userCreateDTO_withInvalidEmailFormat_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("invalid-email");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).containsIgnoringCase("email");
    }

    @Test
    @DisplayName("UserCreateDTO with email exceeding max length should fail validation")
    void userCreateDTO_withEmailExceedingMaxLength_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("a".repeat(45) + "@test.com"); // 54 chars (max is 50)
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).contains("size must be between");
    }

    @Test
    @DisplayName("UserCreateDTO with valid email formats should pass validation")
    void userCreateDTO_withVariousValidEmailFormats_shouldPassValidation() {
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com",
            "123@example.com"
        };

        for (String email : validEmails) {
            UserCreateDTO dto = new UserCreateDTO();
            dto.setPhoneNumber("1234567890");
            dto.setEmail(email);
            dto.setFirstName("John");
            dto.setLastName("Doe");

            Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== FirstName Validation Tests ====================

    @Test
    @DisplayName("UserCreateDTO with null firstName should fail validation")
    void userCreateDTO_withNullFirstName_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName(null);
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    @Test
    @DisplayName("UserCreateDTO with empty firstName should fail validation")
    void userCreateDTO_withEmptyFirstName_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("");
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    @Test
    @DisplayName("UserCreateDTO with firstName exceeding max length should fail validation")
    void userCreateDTO_withFirstNameExceedingMaxLength_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("a".repeat(101)); // 101 chars (max is 100)
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    // ==================== MiddleName Validation Tests ====================

    @Test
    @DisplayName("UserCreateDTO with null middleName should pass validation (optional)")
    void userCreateDTO_withNullMiddleName_shouldPassValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setMiddleName(null);
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserCreateDTO with middleName exceeding max length should fail validation")
    void userCreateDTO_withMiddleNameExceedingMaxLength_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setMiddleName("a".repeat(101)); // 101 chars (max is 100)
        dto.setLastName("Doe");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("middleName");
    }

    // ==================== LastName Validation Tests ====================

    @Test
    @DisplayName("UserCreateDTO with null lastName should fail validation")
    void userCreateDTO_withNullLastName_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName(null);

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastName");
    }

    @Test
    @DisplayName("UserCreateDTO with empty lastName should fail validation")
    void userCreateDTO_withEmptyLastName_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastName");
    }

    @Test
    @DisplayName("UserCreateDTO with lastName exceeding max length should fail validation")
    void userCreateDTO_withLastNameExceedingMaxLength_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("a".repeat(101)); // 101 chars (max is 100)

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastName");
    }

    // ==================== StatusDescription Validation Tests ====================

    @Test
    @DisplayName("UserCreateDTO with statusDescription exceeding max length should fail validation")
    void userCreateDTO_withStatusDescriptionExceedingMaxLength_shouldFailValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setStatusDescription("a".repeat(21)); // 21 chars (max is 20)

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("statusDescription");
    }

    // ==================== Multiple Validation Failures ====================

    @Test
    @DisplayName("UserCreateDTO with multiple invalid fields should report all violations")
    void userCreateDTO_withMultipleInvalidFields_shouldReportAllViolations() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber(""); // blank
        dto.setEmail("invalid-email"); // invalid format
        dto.setFirstName(""); // blank
        dto.setLastName(""); // blank

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(4);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("UserCreateDTO with special characters in names should pass validation")
    void userCreateDTO_withSpecialCharactersInNames_shouldPassValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("Jean-Pierre");
        dto.setMiddleName("O'Connor");
        dto.setLastName("José");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserCreateDTO with future date of birth should pass validation (no date validation)")
    void userCreateDTO_withFutureDateOfBirth_shouldPassValidation() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setPhoneNumber("1234567890");
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setDateOfBirth(LocalDate.now().plusYears(1)); // Future date

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
