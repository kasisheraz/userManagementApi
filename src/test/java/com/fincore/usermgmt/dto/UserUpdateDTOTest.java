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

@DisplayName("UserUpdateDTO Validation Tests")
class UserUpdateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Valid DTO Tests ====================

    @Test
    @DisplayName("Valid UserUpdateDTO with all fields should pass validation")
    void validUserUpdateDTO_withAllFields_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
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

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid UserUpdateDTO with null optional fields should pass validation")
    void validUserUpdateDTO_withNullOptionalFields_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPhoneNumber(null);
        dto.setEmail(null);
        dto.setFirstName(null);
        dto.setMiddleName(null);
        dto.setLastName(null);
        dto.setDateOfBirth(null);
        dto.setResidentialAddressIdentifier(null);
        dto.setPostalAddressIdentifier(null);
        dto.setStatusDescription(null);
        dto.setRole(null);

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid UserUpdateDTO with maximum length fields should pass validation")
    void validUserUpdateDTO_withMaxLengthFields_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPhoneNumber("12345678901234567890"); // 20 chars (max)
        dto.setEmail("a".repeat(40) + "@test.com"); // 50 chars (max)
        dto.setFirstName("a".repeat(100)); // 100 chars (max)
        dto.setMiddleName("b".repeat(100)); // 100 chars (max)
        dto.setLastName("c".repeat(100)); // 100 chars (max)
        dto.setStatusDescription("a".repeat(20)); // 20 chars (max)

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    // ==================== PhoneNumber Validation Tests ====================

    @Test
    @DisplayName("UserUpdateDTO with empty phoneNumber should pass validation (no @NotBlank)")
    void userUpdateDTO_withEmptyPhoneNumber_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPhoneNumber("");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserUpdateDTO with phoneNumber exceeding max length should fail validation")
    void userUpdateDTO_withPhoneNumberExceedingMaxLength_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPhoneNumber("123456789012345678901"); // 21 chars (max is 20)

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("phoneNumber");
        assertThat(violations.iterator().next().getMessage()).contains("size must be between");
    }

    // ==================== Email Validation Tests ====================

    @Test
    @DisplayName("UserUpdateDTO with invalid email format should fail validation")
    void userUpdateDTO_withInvalidEmailFormat_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("invalid-email");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).containsIgnoringCase("email");
    }

    @Test
    @DisplayName("UserUpdateDTO with email exceeding max length should fail validation")
    void userUpdateDTO_withEmailExceedingMaxLength_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("a".repeat(45) + "@test.com"); // 54 chars (max is 50)

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
        assertThat(violations.iterator().next().getMessage()).contains("size must be between");
    }

    @Test
    @DisplayName("UserUpdateDTO with empty email should pass validation")
    void userUpdateDTO_withEmptyEmail_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserUpdateDTO with various valid email formats should pass validation")
    void userUpdateDTO_withVariousValidEmailFormats_shouldPassValidation() {
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example-domain.com",
            "123@example.com"
        };

        for (String email : validEmails) {
            UserUpdateDTO dto = new UserUpdateDTO();
            dto.setEmail(email);

            Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== FirstName Validation Tests ====================

    @Test
    @DisplayName("UserUpdateDTO with empty firstName should pass validation (no @NotBlank)")
    void userUpdateDTO_withEmptyFirstName_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName("");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserUpdateDTO with firstName exceeding max length should fail validation")
    void userUpdateDTO_withFirstNameExceedingMaxLength_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName("a".repeat(101)); // 101 chars (max is 100)

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("firstName");
    }

    // ==================== MiddleName Validation Tests ====================

    @Test
    @DisplayName("UserUpdateDTO with middleName exceeding max length should fail validation")
    void userUpdateDTO_withMiddleNameExceedingMaxLength_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setMiddleName("a".repeat(101)); // 101 chars (max is 100)

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("middleName");
    }

    // ==================== LastName Validation Tests ====================

    @Test
    @DisplayName("UserUpdateDTO with empty lastName should pass validation (no @NotBlank)")
    void userUpdateDTO_withEmptyLastName_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setLastName("");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserUpdateDTO with lastName exceeding max length should fail validation")
    void userUpdateDTO_withLastNameExceedingMaxLength_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setLastName("a".repeat(101)); // 101 chars (max is 100)

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("lastName");
    }

    // ==================== StatusDescription Validation Tests ====================

    @Test
    @DisplayName("UserUpdateDTO with statusDescription exceeding max length should fail validation")
    void userUpdateDTO_withStatusDescriptionExceedingMaxLength_shouldFailValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setStatusDescription("a".repeat(21)); // 21 chars (max is 20)

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("statusDescription");
    }

    // ==================== Multiple Validation Failures ====================

    @Test
    @DisplayName("UserUpdateDTO with multiple invalid fields should report all violations")
    void userUpdateDTO_withMultipleInvalidFields_shouldReportAllViolations() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPhoneNumber("a".repeat(21)); // exceeds max
        dto.setEmail("invalid-email"); // invalid format
        dto.setFirstName("a".repeat(101)); // exceeds max
        dto.setLastName("b".repeat(101)); // exceeds max

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(4);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("UserUpdateDTO with special characters in names should pass validation")
    void userUpdateDTO_withSpecialCharactersInNames_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setFirstName("Jean-Pierre");
        dto.setMiddleName("O'Connor");
        dto.setLastName("José");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserUpdateDTO with whitespace-only fields should pass validation (no @NotBlank)")
    void userUpdateDTO_withWhitespaceOnlyFields_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setPhoneNumber("   ");
        dto.setFirstName("   ");
        dto.setLastName("   ");

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserUpdateDTO with past date of birth should pass validation")
    void userUpdateDTO_withPastDateOfBirth_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserUpdateDTO with future date of birth should pass validation (no date validation)")
    void userUpdateDTO_withFutureDateOfBirth_shouldPassValidation() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setDateOfBirth(LocalDate.now().plusYears(1));

        Set<ConstraintViolation<UserUpdateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
