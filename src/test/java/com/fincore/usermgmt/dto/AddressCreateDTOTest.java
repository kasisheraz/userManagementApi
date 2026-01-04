package com.fincore.usermgmt.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AddressCreateDTO Validation Tests")
class AddressCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Valid DTO Tests ====================

    @Test
    @DisplayName("Valid AddressCreateDTO with all required fields should pass validation")
    void validAddressCreateDTO_withAllRequiredFields_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid AddressCreateDTO with all fields should pass validation")
    void validAddressCreateDTO_withAllFields_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .addressLine2("Apt 4B")
                .postalCode("12345")
                .stateCode("CA")
                .city("Los Angeles")
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid AddressCreateDTO with maximum length fields should pass validation")
    void validAddressCreateDTO_withMaxLengthFields_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("a".repeat(100)) // 100 chars (max)
                .addressLine2("b".repeat(100)) // 100 chars (max)
                .postalCode("c".repeat(20)) // 20 chars (max)
                .stateCode("d".repeat(20)) // 20 chars (max)
                .city("e".repeat(50)) // 50 chars (max)
                .country("f".repeat(50)) // 50 chars (max)
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    // ==================== TypeCode Validation Tests ====================

    @Test
    @DisplayName("AddressCreateDTO with null typeCode should fail validation")
    void addressCreateDTO_withNullTypeCode_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(null)
                .addressLine1("123 Main Street")
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("typeCode");
        assertThat(violations.iterator().next().getMessage()).contains("Address type code is required");
    }

    @Test
    @DisplayName("AddressCreateDTO with zero typeCode should pass validation")
    void addressCreateDTO_withZeroTypeCode_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(0)
                .addressLine1("123 Main Street")
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AddressCreateDTO with negative typeCode should pass validation")
    void addressCreateDTO_withNegativeTypeCode_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(-1)
                .addressLine1("123 Main Street")
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    // ==================== AddressLine1 Validation Tests ====================

    @Test
    @DisplayName("AddressCreateDTO with null addressLine1 should fail validation")
    void addressCreateDTO_withNullAddressLine1_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1(null)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("addressLine1");
        assertThat(violations.iterator().next().getMessage()).contains("Address line 1 is required");
    }

    @Test
    @DisplayName("AddressCreateDTO with empty addressLine1 should fail validation")
    void addressCreateDTO_withEmptyAddressLine1_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("")
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("addressLine1");
    }

    @Test
    @DisplayName("AddressCreateDTO with blank addressLine1 should fail validation")
    void addressCreateDTO_withBlankAddressLine1_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("   ")
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("addressLine1");
    }

    @Test
    @DisplayName("AddressCreateDTO with addressLine1 exceeding max length should fail validation")
    void addressCreateDTO_withAddressLine1ExceedingMaxLength_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("a".repeat(101)) // 101 chars (max is 100)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("addressLine1");
        assertThat(violations.iterator().next().getMessage()).contains("Address line 1 must not exceed 100 characters");
    }

    // ==================== AddressLine2 Validation Tests ====================

    @Test
    @DisplayName("AddressCreateDTO with null addressLine2 should pass validation (optional)")
    void addressCreateDTO_withNullAddressLine2_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .addressLine2(null)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AddressCreateDTO with addressLine2 exceeding max length should fail validation")
    void addressCreateDTO_withAddressLine2ExceedingMaxLength_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .addressLine2("a".repeat(101)) // 101 chars (max is 100)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("addressLine2");
    }

    // ==================== PostalCode Validation Tests ====================

    @Test
    @DisplayName("AddressCreateDTO with null postalCode should pass validation (optional)")
    void addressCreateDTO_withNullPostalCode_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .postalCode(null)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AddressCreateDTO with postalCode exceeding max length should fail validation")
    void addressCreateDTO_withPostalCodeExceedingMaxLength_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .postalCode("a".repeat(21)) // 21 chars (max is 20)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("postalCode");
    }

    // ==================== StateCode Validation Tests ====================

    @Test
    @DisplayName("AddressCreateDTO with stateCode exceeding max length should fail validation")
    void addressCreateDTO_withStateCodeExceedingMaxLength_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .stateCode("a".repeat(21)) // 21 chars (max is 20)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("stateCode");
    }

    // ==================== City Validation Tests ====================

    @Test
    @DisplayName("AddressCreateDTO with city exceeding max length should fail validation")
    void addressCreateDTO_withCityExceedingMaxLength_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .city("a".repeat(51)) // 51 chars (max is 50)
                .country("USA")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("city");
    }

    // ==================== Country Validation Tests ====================

    @Test
    @DisplayName("AddressCreateDTO with null country should fail validation")
    void addressCreateDTO_withNullCountry_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country(null)
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("country");
        assertThat(violations.iterator().next().getMessage()).contains("Country is required");
    }

    @Test
    @DisplayName("AddressCreateDTO with empty country should fail validation")
    void addressCreateDTO_withEmptyCountry_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("country");
    }

    @Test
    @DisplayName("AddressCreateDTO with blank country should fail validation")
    void addressCreateDTO_withBlankCountry_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("   ")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("country");
    }

    @Test
    @DisplayName("AddressCreateDTO with country exceeding max length should fail validation")
    void addressCreateDTO_withCountryExceedingMaxLength_shouldFailValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("a".repeat(51)) // 51 chars (max is 50)
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("country");
    }

    // ==================== Multiple Validation Failures ====================

    @Test
    @DisplayName("AddressCreateDTO with multiple invalid fields should report all violations")
    void addressCreateDTO_withMultipleInvalidFields_shouldReportAllViolations() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(null) // null
                .addressLine1("") // blank
                .country("") // blank
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(3);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("AddressCreateDTO with special characters in address fields should pass validation")
    void addressCreateDTO_withSpecialCharactersInAddressFields_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(1)
                .addressLine1("123 Main St. #4B, Suite 500")
                .addressLine2("c/o John O'Reilly")
                .city("São Paulo")
                .country("Côte d'Ivoire")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AddressCreateDTO with numeric-only fields should pass validation")
    void addressCreateDTO_withNumericOnlyFields_shouldPassValidation() {
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(999)
                .addressLine1("12345")
                .postalCode("67890")
                .stateCode("123")
                .city("456")
                .country("789")
                .build();

        Set<ConstraintViolation<AddressCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
