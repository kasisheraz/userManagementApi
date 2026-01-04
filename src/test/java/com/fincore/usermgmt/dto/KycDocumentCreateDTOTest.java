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

@DisplayName("KycDocumentCreateDTO Validation Tests")
class KycDocumentCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== Valid DTO Tests ====================

    @Test
    @DisplayName("Valid KycDocumentCreateDTO with all required fields should pass validation")
    void validKycDocumentCreateDTO_withAllRequiredFields_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PASSPORT")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid KycDocumentCreateDTO with all fields should pass validation")
    void validKycDocumentCreateDTO_withAllFields_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .verificationIdentifier(123)
                .documentType("PASSPORT")
                .sumsubDocumentIdentifier("SUMSUB123456")
                .fileName("passport_scan.pdf")
                .fileUrl("https://example.com/documents/passport_scan.pdf")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Valid KycDocumentCreateDTO with maximum length fields should pass validation")
    void validKycDocumentCreateDTO_withMaxLengthFields_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("ID_CARD")
                .sumsubDocumentIdentifier("a".repeat(100)) // 100 chars (max)
                .fileName("b".repeat(255)) // 255 chars (max)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    // ==================== OrganisationId Validation Tests ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with null organisationId should fail validation")
    void kycDocumentCreateDTO_withNullOrganisationId_shouldFailValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(null)
                .documentType("PASSPORT")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("organisationId");
        assertThat(violations.iterator().next().getMessage()).contains("Organisation ID is required");
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with zero organisationId should pass validation")
    void kycDocumentCreateDTO_withZeroOrganisationId_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(0L)
                .documentType("PASSPORT")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with negative organisationId should pass validation")
    void kycDocumentCreateDTO_withNegativeOrganisationId_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(-1L)
                .documentType("PASSPORT")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with large organisationId should pass validation")
    void kycDocumentCreateDTO_withLargeOrganisationId_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(Long.MAX_VALUE)
                .documentType("PASSPORT")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    // ==================== VerificationIdentifier Validation Tests ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with null verificationIdentifier should pass validation (optional)")
    void kycDocumentCreateDTO_withNullVerificationIdentifier_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .verificationIdentifier(null)
                .documentType("PASSPORT")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    // ==================== DocumentType Validation Tests ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with null documentType should fail validation")
    void kycDocumentCreateDTO_withNullDocumentType_shouldFailValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType(null)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("documentType");
        assertThat(violations.iterator().next().getMessage()).contains("Document type is required");
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with empty documentType should fail validation")
    void kycDocumentCreateDTO_withEmptyDocumentType_shouldFailValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("documentType");
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with blank documentType should fail validation")
    void kycDocumentCreateDTO_withBlankDocumentType_shouldFailValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("   ")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("documentType");
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with various valid documentType values should pass validation")
    void kycDocumentCreateDTO_withVariousValidDocumentTypes_shouldPassValidation() {
        String[] validDocumentTypes = {
            "PASSPORT",
            "ID_CARD",
            "DRIVERS_LICENSE",
            "PROOF_OF_ADDRESS",
            "BANK_STATEMENT",
            "UTILITY_BILL"
        };

        for (String documentType : validDocumentTypes) {
            KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                    .organisationId(1L)
                    .documentType(documentType)
                    .build();

            Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== SumsubDocumentIdentifier Validation Tests ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with null sumsubDocumentIdentifier should pass validation (optional)")
    void kycDocumentCreateDTO_withNullSumsubDocumentIdentifier_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PASSPORT")
                .sumsubDocumentIdentifier(null)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with sumsubDocumentIdentifier exceeding max length should fail validation")
    void kycDocumentCreateDTO_withSumsubDocumentIdentifierExceedingMaxLength_shouldFailValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PASSPORT")
                .sumsubDocumentIdentifier("a".repeat(101)) // 101 chars (max is 100)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("sumsubDocumentIdentifier");
        assertThat(violations.iterator().next().getMessage()).contains("Sumsub document identifier must not exceed 100 characters");
    }

    // ==================== FileName Validation Tests ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with null fileName should pass validation (optional)")
    void kycDocumentCreateDTO_withNullFileName_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PASSPORT")
                .fileName(null)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with fileName exceeding max length should fail validation")
    void kycDocumentCreateDTO_withFileNameExceedingMaxLength_shouldFailValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PASSPORT")
                .fileName("a".repeat(256)) // 256 chars (max is 255)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("fileName");
        assertThat(violations.iterator().next().getMessage()).contains("File name must not exceed 255 characters");
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with various valid fileName formats should pass validation")
    void kycDocumentCreateDTO_withVariousValidFileNameFormats_shouldPassValidation() {
        String[] validFileNames = {
            "document.pdf",
            "passport_scan.jpg",
            "utility-bill-2024.png",
            "Bank Statement.pdf",
            "ID_Card_Front.jpeg"
        };

        for (String fileName : validFileNames) {
            KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                    .organisationId(1L)
                    .documentType("PASSPORT")
                    .fileName(fileName)
                    .build();

            Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== FileUrl Validation Tests ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with null fileUrl should pass validation (optional)")
    void kycDocumentCreateDTO_withNullFileUrl_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PASSPORT")
                .fileUrl(null)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with various valid fileUrl formats should pass validation")
    void kycDocumentCreateDTO_withVariousValidFileUrlFormats_shouldPassValidation() {
        String[] validFileUrls = {
            "https://example.com/document.pdf",
            "http://storage.example.com/kyc/passport.jpg",
            "https://cdn.example.com/uploads/2024/01/document.png",
            "https://api.example.com/v1/documents/12345"
        };

        for (String fileUrl : validFileUrls) {
            KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                    .organisationId(1L)
                    .documentType("PASSPORT")
                    .fileUrl(fileUrl)
                    .build();

            Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

            assertThat(violations).isEmpty();
        }
    }

    // ==================== Multiple Validation Failures ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with multiple invalid fields should report all violations")
    void kycDocumentCreateDTO_withMultipleInvalidFields_shouldReportAllViolations() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(null) // null
                .documentType("") // blank
                .sumsubDocumentIdentifier("a".repeat(101)) // exceeds max
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(3);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("KycDocumentCreateDTO with special characters in fields should pass validation")
    void kycDocumentCreateDTO_withSpecialCharactersInFields_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PROOF_OF_ADDRESS")
                .sumsubDocumentIdentifier("SUMSUB-12345-ABCDE")
                .fileName("Utility Bill (2024-01).pdf")
                .fileUrl("https://example.com/docs/user@email.com/file.pdf")
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("KycDocumentCreateDTO with empty optional fields should pass validation")
    void kycDocumentCreateDTO_withEmptyOptionalFields_shouldPassValidation() {
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .verificationIdentifier(null)
                .documentType("PASSPORT")
                .sumsubDocumentIdentifier(null)
                .fileName(null)
                .fileUrl(null)
                .build();

        Set<ConstraintViolation<KycDocumentCreateDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
