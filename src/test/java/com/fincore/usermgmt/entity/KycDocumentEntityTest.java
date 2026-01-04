package com.fincore.usermgmt.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for KycDocument entity lifecycle methods and JPA behavior.
 * Tests @PrePersist, @PreUpdate callbacks, entity relationships, and status workflow.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("KycDocument Entity Tests")
class KycDocumentEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    private Organisation testOrganisation;
    private User verifierUser;

    @BeforeEach
    void setUp() {
        // Create test role
        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("Admin role");
        entityManager.persist(role);

        // Create owner user
        User owner = new User();
        owner.setPhoneNumber("+1234567890");
        owner.setFirstName("Owner");
        owner.setLastName("User");
        owner.setRole(role);
        entityManager.persist(owner);

        // Create verifier user
        verifierUser = new User();
        verifierUser.setPhoneNumber("+9876543210");
        verifierUser.setFirstName("Verifier");
        verifierUser.setLastName("User");
        verifierUser.setRole(role);
        entityManager.persist(verifierUser);

        // Create test organisation
        testOrganisation = Organisation.builder()
                .owner(owner)
                .legalName("Test Organisation Ltd")
                .organisationType(OrganisationType.LTD)
                .build();
        entityManager.persist(testOrganisation);
        entityManager.flush();
    }

    @Test
    @DisplayName("@PrePersist should set timestamps and default status on new document")
    void prePersist_shouldSetTimestampsAndDefaultStatus() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();

        // Act
        LocalDateTime beforePersist = LocalDateTime.now().minusSeconds(1);
        KycDocument savedDoc = entityManager.persistAndFlush(doc);
        LocalDateTime afterPersist = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(savedDoc.getCreatedDatetime()).isNotNull();
        assertThat(savedDoc.getLastModifiedDatetime()).isNotNull();
        assertThat(savedDoc.getCreatedDatetime()).isBetween(beforePersist, afterPersist);
        assertThat(savedDoc.getLastModifiedDatetime()).isBetween(beforePersist, afterPersist);
        assertThat(savedDoc.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("@PreUpdate should update lastModifiedDatetime on document update")
    void preUpdate_shouldUpdateLastModifiedDatetime() throws InterruptedException {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();
        KycDocument savedDoc = entityManager.persistAndFlush(doc);
        
        LocalDateTime originalCreated = savedDoc.getCreatedDatetime();
        LocalDateTime originalModified = savedDoc.getLastModifiedDatetime();
        
        Thread.sleep(100);

        // Act
        savedDoc.setFileName("updated_passport.pdf");
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        entityManager.persistAndFlush(savedDoc);
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(savedDoc.getCreatedDatetime()).isEqualTo(originalCreated);
        assertThat(savedDoc.getLastModifiedDatetime()).isNotEqualTo(originalModified);
        assertThat(savedDoc.getLastModifiedDatetime()).isBetween(beforeUpdate, afterUpdate);
    }

    @Test
    @DisplayName("KycDocument should persist with all required fields")
    void persist_withRequiredFields_shouldSucceed() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);
        entityManager.clear();
        KycDocument retrieved = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getDocumentType()).isEqualTo(DocumentType.PASSPORT);
        assertThat(retrieved.getOrganisation().getId()).isEqualTo(testOrganisation.getId());
    }

    @Test
    @DisplayName("KycDocument should persist with comprehensive details")
    void persist_withComprehensiveDetails_shouldSucceed() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .verificationIdentifier(12345)
                .documentType(DocumentType.CERTIFICATE_OF_INCORPORATION)
                .sumsubDocumentIdentifier("SUMSUB_DOC_123456")
                .fileName("company_certificate.pdf")
                .fileUrl("https://example.com/docs/certificate.pdf")
                .status(DocumentStatus.VERIFIED)
                .reasonDescription("Document verified successfully")
                .verifiedBy(verifierUser)
                .createdBy(1L)
                .lastModifiedBy(1L)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);
        entityManager.clear();
        KycDocument retrieved = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getVerificationIdentifier()).isEqualTo(12345);
        assertThat(retrieved.getDocumentType()).isEqualTo(DocumentType.CERTIFICATE_OF_INCORPORATION);
        assertThat(retrieved.getSumsubDocumentIdentifier()).isEqualTo("SUMSUB_DOC_123456");
        assertThat(retrieved.getFileName()).isEqualTo("company_certificate.pdf");
        assertThat(retrieved.getFileUrl()).isEqualTo("https://example.com/docs/certificate.pdf");
        assertThat(retrieved.getStatus()).isEqualTo(DocumentStatus.VERIFIED);
        assertThat(retrieved.getReasonDescription()).isEqualTo("Document verified successfully");
        assertThat(retrieved.getCreatedBy()).isEqualTo(1L);
        assertThat(retrieved.getLastModifiedBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("KycDocument should maintain relationship with Organisation")
    void persist_shouldMaintainOrganisationRelationship() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);
        entityManager.clear();
        KycDocument retrieved = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(retrieved.getOrganisation()).isNotNull();
        assertThat(retrieved.getOrganisation().getId()).isEqualTo(testOrganisation.getId());
        assertThat(retrieved.getOrganisation().getLegalName()).isEqualTo("Test Organisation Ltd");
    }

    @Test
    @DisplayName("KycDocument should maintain relationship with verifier User")
    void persist_shouldMaintainVerifierRelationship() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .verifiedBy(verifierUser)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);
        entityManager.clear();
        KycDocument retrieved = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(retrieved.getVerifiedBy()).isNotNull();
        assertThat(retrieved.getVerifiedBy().getId()).isEqualTo(verifierUser.getId());
        assertThat(retrieved.getVerifiedBy().getFirstName()).isEqualTo("Verifier");
    }

    @Test
    @DisplayName("KycDocument status should default to PENDING if not set")
    void persist_withoutStatus_shouldDefaultToPending() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);

        // Assert
        assertThat(savedDoc.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    @Test
    @DisplayName("KycDocument status should preserve explicit value")
    void persist_withExplicitStatus_shouldPreserveStatus() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .status(DocumentStatus.VERIFIED)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);

        // Assert
        assertThat(savedDoc.getStatus()).isEqualTo(DocumentStatus.VERIFIED);
    }

    @Test
    @DisplayName("KycDocument can be updated to VERIFIED status")
    void update_toVerifiedStatus_shouldSucceed() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();
        KycDocument savedDoc = entityManager.persistAndFlush(doc);

        // Act
        savedDoc.setStatus(DocumentStatus.VERIFIED);
        savedDoc.setVerifiedBy(verifierUser);
        savedDoc.setReasonDescription("Document verified successfully");
        entityManager.persistAndFlush(savedDoc);
        entityManager.clear();
        KycDocument updated = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(updated.getStatus()).isEqualTo(DocumentStatus.VERIFIED);
        assertThat(updated.getVerifiedBy()).isNotNull();
        assertThat(updated.getReasonDescription()).isEqualTo("Document verified successfully");
    }

    @Test
    @DisplayName("KycDocument can be updated to REJECTED status")
    void update_toRejectedStatus_shouldSucceed() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();
        KycDocument savedDoc = entityManager.persistAndFlush(doc);

        // Act
        savedDoc.setStatus(DocumentStatus.REJECTED);
        savedDoc.setReasonDescription("Document quality is poor");
        entityManager.persistAndFlush(savedDoc);
        entityManager.clear();
        KycDocument updated = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(updated.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(updated.getReasonDescription()).isEqualTo("Document quality is poor");
    }

    @Test
    @DisplayName("Multiple KycDocuments can belong to same Organisation")
    void persist_multipleDocumentsForSameOrganisation_shouldSucceed() {
        // Arrange
        KycDocument doc1 = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();

        KycDocument doc2 = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.CERTIFICATE_OF_INCORPORATION)
                .build();

        // Act
        KycDocument saved1 = entityManager.persistAndFlush(doc1);
        KycDocument saved2 = entityManager.persistAndFlush(doc2);

        // Assert
        assertThat(saved1.getOrganisation().getId()).isEqualTo(saved2.getOrganisation().getId());
    }

    @Test
    @DisplayName("KycDocument can be created with different document types")
    void persist_withDifferentDocumentTypes_shouldSucceed() {
        // Arrange & Act
        KycDocument passport = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();
        KycDocument savedPassport = entityManager.persistAndFlush(passport);

        KycDocument idCard = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.ID_DOCUMENT)
                .build();
        KycDocument savedIdCard = entityManager.persistAndFlush(idCard);

        KycDocument certificate = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.CERTIFICATE_OF_INCORPORATION)
                .build();
        KycDocument savedCertificate = entityManager.persistAndFlush(certificate);

        // Assert
        assertThat(savedPassport.getDocumentType()).isEqualTo(DocumentType.PASSPORT);
        assertThat(savedIdCard.getDocumentType()).isEqualTo(DocumentType.ID_DOCUMENT);
        assertThat(savedCertificate.getDocumentType()).isEqualTo(DocumentType.CERTIFICATE_OF_INCORPORATION);
    }

    @Test
    @DisplayName("KycDocument timestamps should not be null after persistence")
    void persist_timestampsShouldNeverBeNull() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);

        // Assert
        assertThat(savedDoc.getCreatedDatetime()).as("Created datetime should not be null").isNotNull();
        assertThat(savedDoc.getLastModifiedDatetime()).as("Last modified datetime should not be null").isNotNull();
    }

    @Test
    @DisplayName("KycDocument can handle null optional fields")
    void persist_withNullOptionalFields_shouldSucceed() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .verificationIdentifier(null)
                .sumsubDocumentIdentifier(null)
                .fileName(null)
                .fileUrl(null)
                .verifiedBy(null)
                .build();

        // Act
        KycDocument savedDoc = entityManager.persistAndFlush(doc);
        entityManager.clear();
        KycDocument retrieved = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getVerificationIdentifier()).isNull();
        assertThat(retrieved.getSumsubDocumentIdentifier()).isNull();
        assertThat(retrieved.getFileName()).isNull();
        assertThat(retrieved.getFileUrl()).isNull();
        assertThat(retrieved.getVerifiedBy()).isNull();
    }

    @Test
    @DisplayName("KycDocument can update file metadata")
    void update_fileMetadata_shouldSucceed() {
        // Arrange
        KycDocument doc = KycDocument.builder()
                .organisation(testOrganisation)
                .documentType(DocumentType.PASSPORT)
                .build();
        KycDocument savedDoc = entityManager.persistAndFlush(doc);

        // Act
        savedDoc.setFileName("new_passport.pdf");
        savedDoc.setFileUrl("https://example.com/new_passport.pdf");
        savedDoc.setSumsubDocumentIdentifier("SUMSUB_NEW_123");
        entityManager.persistAndFlush(savedDoc);
        entityManager.clear();
        KycDocument updated = entityManager.find(KycDocument.class, savedDoc.getId());

        // Assert
        assertThat(updated.getFileName()).isEqualTo("new_passport.pdf");
        assertThat(updated.getFileUrl()).isEqualTo("https://example.com/new_passport.pdf");
        assertThat(updated.getSumsubDocumentIdentifier()).isEqualTo("SUMSUB_NEW_123");
    }
}
