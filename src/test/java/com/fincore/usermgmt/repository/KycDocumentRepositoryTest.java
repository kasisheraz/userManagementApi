package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class KycDocumentRepositoryTest {

    @Autowired
    private KycDocumentRepository kycDocumentRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Organisation organisation;
    private User verifier;
    private KycDocument kycDocument;

    @BeforeEach
    void setUp() {
        // Create role
        Role role = new Role();
        role.setName("TEST_ROLE");
        role.setDescription("Test Role for Repository Tests");
        role = roleRepository.save(role);

        // Create owner user with unique phone number
        User owner = new User();
        owner.setPhoneNumber("+9999990002");
        owner.setEmail("owner.kyc.test@test.com");
        owner.setFirstName("Test");
        owner.setLastName("Owner");
        owner.setRole(role);
        owner.setStatusDescription("ACTIVE");
        owner = userRepository.save(owner);

        // Create verifier user with unique phone number
        verifier = new User();
        verifier.setPhoneNumber("+9999990003");
        verifier.setEmail("verifier.kyc.test@test.com");
        verifier.setFirstName("Test");
        verifier.setLastName("Verifier");
        verifier.setRole(role);
        verifier.setStatusDescription("ACTIVE");
        verifier = userRepository.save(verifier);

        // Create organisation
        organisation = Organisation.builder()
                .owner(owner)
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType(OrganisationType.LTD)
                .status(OrganisationStatus.PENDING)
                .registrationNumber("12345678")
                .build();
        organisation = organisationRepository.save(organisation);

        // Create KYC document
        kycDocument = KycDocument.builder()
                .organisation(organisation)
                .documentType(DocumentType.CERTIFICATE_OF_INCORPORATION)
                .fileName("certificate.pdf")
                .fileUrl("https://storage.example.com/certificate.pdf")
                .status(DocumentStatus.PENDING)
                .sumsubDocumentIdentifier("SUMSUB123")
                .build();
        kycDocument = kycDocumentRepository.save(kycDocument);
    }

    @Test
    void findByOrganisationId_Found() {
        List<KycDocument> result = kycDocumentRepository.findByOrganisationId(organisation.getId());
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("certificate.pdf", result.get(0).getFileName());
    }

    @Test
    void findByOrganisationId_NotFound() {
        List<KycDocument> result = kycDocumentRepository.findByOrganisationId(999L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void findByOrganisationIdPaged_Found() {
        Page<KycDocument> result = kycDocumentRepository.findByOrganisationId(
                organisation.getId(), PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByStatus_Found() {
        List<KycDocument> result = kycDocumentRepository.findByStatus(DocumentStatus.PENDING);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByDocumentType_Found() {
        List<KycDocument> result = kycDocumentRepository.findByDocumentType(DocumentType.CERTIFICATE_OF_INCORPORATION);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByOrganisationIdAndDocumentType_Found() {
        List<KycDocument> result = kycDocumentRepository.findByOrganisationIdAndDocumentType(
                organisation.getId(), DocumentType.CERTIFICATE_OF_INCORPORATION);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByOrganisationIdAndStatus_Found() {
        List<KycDocument> result = kycDocumentRepository.findByOrganisationIdAndStatus(
                organisation.getId(), DocumentStatus.PENDING);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findBySumsubDocumentIdentifier_Found() {
        Optional<KycDocument> result = kycDocumentRepository.findBySumsubDocumentIdentifier("SUMSUB123");
        
        assertTrue(result.isPresent());
        assertEquals("certificate.pdf", result.get().getFileName());
    }

    @Test
    void findBySumsubDocumentIdentifier_NotFound() {
        Optional<KycDocument> result = kycDocumentRepository.findBySumsubDocumentIdentifier("NONEXISTENT");
        
        assertFalse(result.isPresent());
    }

    @Test
    void findPendingVerification_Found() {
        List<KycDocument> result = kycDocumentRepository.findPendingVerification();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void countByOrganisationIdAndStatus_Success() {
        long count = kycDocumentRepository.countByOrganisationIdAndStatus(
                organisation.getId(), DocumentStatus.PENDING);
        
        assertEquals(1, count);
    }

    @Test
    void countVerifiedDocumentsByOrganisation_None() {
        long count = kycDocumentRepository.countVerifiedDocumentsByOrganisation(organisation.getId());
        
        assertEquals(0, count);
    }

    @Test
    void countVerifiedDocumentsByOrganisation_WithVerified() {
        // Update document to verified
        kycDocument.setStatus(DocumentStatus.VERIFIED);
        kycDocument.setVerifiedBy(verifier);
        kycDocumentRepository.save(kycDocument);

        long count = kycDocumentRepository.countVerifiedDocumentsByOrganisation(organisation.getId());
        
        assertEquals(1, count);
    }

    @Test
    void findPendingVerification_WithUnderReview() {
        // Add document under review
        KycDocument underReviewDoc = KycDocument.builder()
                .organisation(organisation)
                .documentType(DocumentType.PROOF_OF_ADDRESS)
                .fileName("proof.pdf")
                .status(DocumentStatus.UNDER_REVIEW)
                .build();
        kycDocumentRepository.save(underReviewDoc);

        List<KycDocument> result = kycDocumentRepository.findPendingVerification();
        
        assertEquals(2, result.size());
    }
}
