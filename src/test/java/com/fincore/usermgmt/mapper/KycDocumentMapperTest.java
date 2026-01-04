package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.KycDocumentCreateDTO;
import com.fincore.usermgmt.dto.KycDocumentDTO;
import com.fincore.usermgmt.dto.KycDocumentUpdateDTO;
import com.fincore.usermgmt.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class KycDocumentMapperTest {

    private KycDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(KycDocumentMapper.class);
    }

    @Test
    void toKycDocumentDTO_shouldMapAllFields() {
        // Given
        Organisation organisation = Organisation.builder()
                .id(1L)
                .legalName("Test Company Ltd")
                .build();

        User verifiedBy = new User();
        verifiedBy.setId(2L);
        verifiedBy.setFirstName("Jane");
        verifiedBy.setLastName("Verifier");

        KycDocument document = KycDocument.builder()
                .id(100L)
                .organisation(organisation)
                .documentType(DocumentType.CERTIFICATE_OF_INCORPORATION)
                .fileName("cert.pdf")
                .fileUrl("https://storage.example.com/cert.pdf")
                .status(DocumentStatus.VERIFIED)
                .verifiedBy(verifiedBy)
                .createdDatetime(LocalDateTime.now())
                .build();

        // When
        KycDocumentDTO dto = mapper.toKycDocumentDTO(document);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getOrganisationId()).isEqualTo(1L);
        assertThat(dto.getOrganisationName()).isEqualTo("Test Company Ltd");
        assertThat(dto.getDocumentType()).isEqualTo("CERTIFICATE_OF_INCORPORATION");
        assertThat(dto.getFileName()).isEqualTo("cert.pdf");
        assertThat(dto.getFileUrl()).isEqualTo("https://storage.example.com/cert.pdf");
        assertThat(dto.getStatus()).isEqualTo("VERIFIED");
        assertThat(dto.getVerifiedById()).isEqualTo(2L);
        assertThat(dto.getVerifiedByName()).isEqualTo("Jane Verifier");
    }

    @Test
    void toKycDocumentDTO_withNullVerifier_shouldReturnNull() {
        // Given
        Organisation organisation = Organisation.builder()
                .id(1L)
                .legalName("Test Company")
                .build();

        KycDocument document = KycDocument.builder()
                .id(100L)
                .organisation(organisation)
                .documentType(DocumentType.BANK_STATEMENT)
                .status(DocumentStatus.PENDING)
                .build();

        // When
        KycDocumentDTO dto = mapper.toKycDocumentDTO(document);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getVerifiedById()).isNull();
        assertThat(dto.getVerifiedByName()).isNull();
    }

    @Test
    void toKycDocumentDTO_withVerifierMissingNames_shouldReturnEmptyString() {
        // Given
        User verifiedBy = new User();
        verifiedBy.setId(2L);

        KycDocument document = KycDocument.builder()
                .id(100L)
                .verifiedBy(verifiedBy)
                .status(DocumentStatus.VERIFIED)
                .build();

        // When
        KycDocumentDTO dto = mapper.toKycDocumentDTO(document);

        // Then
        assertThat(dto.getVerifiedByName()).isEmpty();
    }

    @Test
    void toKycDocument_shouldMapCreateDTO() {
        // Given
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("PROOF_OF_ADDRESS")
                .fileName("address_proof.pdf")
                .fileUrl("https://storage.example.com/address.pdf")
                .build();

        // When
        KycDocument document = mapper.toKycDocument(dto);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getId()).isNull();
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.PROOF_OF_ADDRESS);
        assertThat(document.getFileName()).isEqualTo("address_proof.pdf");
        assertThat(document.getFileUrl()).isEqualTo("https://storage.example.com/address.pdf");
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    void updateKycDocumentFromDto_shouldUpdateNonNullFields() {
        // Given
        KycDocument existing = KycDocument.builder()
                .id(100L)
                .fileName("old.pdf")
                .fileUrl("https://old.com/file.pdf")
                .status(DocumentStatus.PENDING)
                .build();

        KycDocumentUpdateDTO updateDTO = KycDocumentUpdateDTO.builder()
                .fileName("updated.pdf")
                .fileUrl("https://new.com/file.pdf")
                .build();

        // When
        mapper.updateKycDocumentFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getFileName()).isEqualTo("updated.pdf");
        assertThat(existing.getFileUrl()).isEqualTo("https://new.com/file.pdf");
        assertThat(existing.getStatus()).isEqualTo(DocumentStatus.PENDING); // Not updated
    }

    @Test
    void getVerifierFullName_withBothNames_shouldConcatenate() {
        // Given
        User verifier = new User();
        verifier.setFirstName("John");
        verifier.setLastName("Compliance");

        KycDocument document = KycDocument.builder()
                .verifiedBy(verifier)
                .build();

        // When
        String fullName = mapper.getVerifierFullName(document);

        // Then
        assertThat(fullName).isEqualTo("John Compliance");
    }

    @Test
    void getVerifierFullName_withFirstNameOnly_shouldReturnFirstName() {
        // Given
        User verifier = new User();
        verifier.setFirstName("John");

        KycDocument document = KycDocument.builder()
                .verifiedBy(verifier)
                .build();

        // When
        String fullName = mapper.getVerifierFullName(document);

        // Then
        assertThat(fullName).isEqualTo("John");
    }

    @Test
    void getVerifierFullName_withNullVerifier_shouldReturnNull() {
        // Given
        KycDocument document = KycDocument.builder().build();

        // When
        String fullName = mapper.getVerifierFullName(document);

        // Then
        assertThat(fullName).isNull();
    }

    @Test
    void documentTypeToString_withValidType_shouldReturnName() {
        // When
        String result = mapper.documentTypeToString(DocumentType.BANK_STATEMENT);

        // Then
        assertThat(result).isEqualTo("BANK_STATEMENT");
    }

    @Test
    void documentTypeToString_withNull_shouldReturnNull() {
        // When
        String result = mapper.documentTypeToString(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToDocumentType_withValidString_shouldReturnEnum() {
        // When
        DocumentType result = mapper.stringToDocumentType("CERTIFICATE_OF_INCORPORATION");

        // Then
        assertThat(result).isEqualTo(DocumentType.CERTIFICATE_OF_INCORPORATION);
    }

    @Test
    void stringToDocumentType_withLowerCase_shouldReturnEnum() {
        // When
        DocumentType result = mapper.stringToDocumentType("bank_statement");

        // Then
        assertThat(result).isEqualTo(DocumentType.BANK_STATEMENT);
    }

    @Test
    void stringToDocumentType_withNull_shouldReturnNull() {
        // When
        DocumentType result = mapper.stringToDocumentType(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToDocumentType_withEmpty_shouldReturnNull() {
        // When
        DocumentType result = mapper.stringToDocumentType("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToDocumentType_withInvalidString_shouldReturnOther() {
        // When
        DocumentType result = mapper.stringToDocumentType("INVALID_TYPE");

        // Then
        assertThat(result).isEqualTo(DocumentType.OTHER);
    }

    @Test
    void statusToString_withValidStatus_shouldReturnName() {
        // When
        String result = mapper.statusToString(DocumentStatus.VERIFIED);

        // Then
        assertThat(result).isEqualTo("VERIFIED");
    }

    @Test
    void statusToString_withNull_shouldReturnNull() {
        // When
        String result = mapper.statusToString(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToStatus_withValidString_shouldReturnEnum() {
        // When
        DocumentStatus result = mapper.stringToStatus("PENDING");

        // Then
        assertThat(result).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    void stringToStatus_withLowerCase_shouldReturnEnum() {
        // When
        DocumentStatus result = mapper.stringToStatus("verified");

        // Then
        assertThat(result).isEqualTo(DocumentStatus.VERIFIED);
    }

    @Test
    void stringToStatus_withNull_shouldReturnNull() {
        // When
        DocumentStatus result = mapper.stringToStatus(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToStatus_withEmpty_shouldReturnNull() {
        // When
        DocumentStatus result = mapper.stringToStatus("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToStatus_withInvalidString_shouldReturnNull() {
        // When
        DocumentStatus result = mapper.stringToStatus("INVALID_STATUS");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toKycDocument_withOtherDocumentType_shouldMapCorrectly() {
        // Given
        KycDocumentCreateDTO dto = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("OTHER")
                .fileName("other.pdf")
                .fileUrl("https://storage.example.com/other.pdf")
                .build();

        // When
        KycDocument document = mapper.toKycDocument(dto);

        // Then
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.OTHER);
    }

    @Test
    void toKycDocumentDTO_withAllDocumentTypes_shouldMapCorrectly() {
        // Test all enum values
        for (DocumentType type : DocumentType.values()) {
            KycDocument document = KycDocument.builder()
                    .id(1L)
                    .documentType(type)
                    .status(DocumentStatus.PENDING)
                    .build();

            KycDocumentDTO dto = mapper.toKycDocumentDTO(document);

            assertThat(dto.getDocumentType()).isEqualTo(type.name());
        }
    }

    @Test
    void toKycDocumentDTO_withAllStatuses_shouldMapCorrectly() {
        // Test all enum values
        for (DocumentStatus status : DocumentStatus.values()) {
            KycDocument document = KycDocument.builder()
                    .id(1L)
                    .documentType(DocumentType.OTHER)
                    .status(status)
                    .build();

            KycDocumentDTO dto = mapper.toKycDocumentDTO(document);

            assertThat(dto.getStatus()).isEqualTo(status.name());
        }
    }
}
