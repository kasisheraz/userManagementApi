package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.KycDocumentCreateDTO;
import com.fincore.usermgmt.dto.KycDocumentDTO;
import com.fincore.usermgmt.dto.KycDocumentUpdateDTO;
import com.fincore.usermgmt.dto.PagedResponse;
import com.fincore.usermgmt.entity.*;
import com.fincore.usermgmt.mapper.KycDocumentMapper;
import com.fincore.usermgmt.repository.KycDocumentRepository;
import com.fincore.usermgmt.repository.OrganisationRepository;
import com.fincore.usermgmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KycDocumentServiceTest {

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KycDocumentMapper kycDocumentMapper;

    @InjectMocks
    private KycDocumentService kycDocumentService;

    private Organisation organisation;
    private User verifier;
    private KycDocument kycDocument;
    private KycDocumentDTO kycDocumentDTO;
    private KycDocumentCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        organisation = Organisation.builder()
                .id(1L)
                .legalName("Test Company Ltd")
                .status(OrganisationStatus.PENDING)
                .build();

        verifier = new User();
        verifier.setId(2L);
        verifier.setFirstName("Admin");
        verifier.setLastName("User");

        kycDocument = KycDocument.builder()
                .id(1L)
                .organisation(organisation)
                .documentType(DocumentType.CERTIFICATE_OF_INCORPORATION)
                .fileName("certificate.pdf")
                .fileUrl("https://storage.example.com/certificate.pdf")
                .status(DocumentStatus.PENDING)
                .build();

        kycDocumentDTO = KycDocumentDTO.builder()
                .id(1L)
                .organisationId(1L)
                .organisationName("Test Company Ltd")
                .documentType("CERTIFICATE_OF_INCORPORATION")
                .fileName("certificate.pdf")
                .fileUrl("https://storage.example.com/certificate.pdf")
                .status("PENDING")
                .build();

        createDTO = KycDocumentCreateDTO.builder()
                .organisationId(1L)
                .documentType("CERTIFICATE_OF_INCORPORATION")
                .fileName("certificate.pdf")
                .fileUrl("https://storage.example.com/certificate.pdf")
                .build();
    }

    @Test
    void createDocument_Success() {
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(organisation));
        when(kycDocumentMapper.toKycDocument(any(KycDocumentCreateDTO.class))).thenReturn(kycDocument);
        when(kycDocumentRepository.save(any(KycDocument.class))).thenReturn(kycDocument);
        when(kycDocumentMapper.toKycDocumentDTO(any(KycDocument.class))).thenReturn(kycDocumentDTO);

        KycDocumentDTO result = kycDocumentService.createDocument(createDTO);

        assertNotNull(result);
        assertEquals("CERTIFICATE_OF_INCORPORATION", result.getDocumentType());
        assertEquals(1L, result.getOrganisationId());
        verify(kycDocumentRepository).save(any(KycDocument.class));
    }

    @Test
    void createDocument_OrganisationNotFound() {
        when(organisationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            kycDocumentService.createDocument(createDTO));
        
        assertTrue(exception.getMessage().contains("Organisation not found"));
    }

    @Test
    void getDocumentById_Found() {
        when(kycDocumentRepository.findById(1L)).thenReturn(Optional.of(kycDocument));
        when(kycDocumentMapper.toKycDocumentDTO(kycDocument)).thenReturn(kycDocumentDTO);

        Optional<KycDocumentDTO> result = kycDocumentService.getDocumentById(1L);

        assertTrue(result.isPresent());
        assertEquals("CERTIFICATE_OF_INCORPORATION", result.get().getDocumentType());
    }

    @Test
    void getDocumentById_NotFound() {
        when(kycDocumentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<KycDocumentDTO> result = kycDocumentService.getDocumentById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getDocumentsByOrganisation_Success() {
        when(kycDocumentRepository.findByOrganisationId(1L)).thenReturn(Arrays.asList(kycDocument));
        when(kycDocumentMapper.toKycDocumentDTO(kycDocument)).thenReturn(kycDocumentDTO);

        List<KycDocumentDTO> result = kycDocumentService.getDocumentsByOrganisation(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getDocumentsByOrganisationPaged_Success() {
        Page<KycDocument> page = new PageImpl<>(Arrays.asList(kycDocument));
        when(kycDocumentRepository.findByOrganisationId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(kycDocumentMapper.toKycDocumentDTO(kycDocument)).thenReturn(kycDocumentDTO);

        PagedResponse<KycDocumentDTO> result = kycDocumentService.getDocumentsByOrganisationPaged(1L, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getDocumentsByStatus_Success() {
        when(kycDocumentRepository.findByStatus(DocumentStatus.PENDING)).thenReturn(Arrays.asList(kycDocument));
        when(kycDocumentMapper.toKycDocumentDTO(kycDocument)).thenReturn(kycDocumentDTO);

        List<KycDocumentDTO> result = kycDocumentService.getDocumentsByStatus("PENDING");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getPendingDocuments_Success() {
        when(kycDocumentRepository.findPendingVerification()).thenReturn(Arrays.asList(kycDocument));
        when(kycDocumentMapper.toKycDocumentDTO(kycDocument)).thenReturn(kycDocumentDTO);

        List<KycDocumentDTO> result = kycDocumentService.getPendingDocuments();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateDocument_Success() {
        KycDocumentUpdateDTO updateDTO = KycDocumentUpdateDTO.builder()
                .fileName("updated_certificate.pdf")
                .build();

        when(kycDocumentRepository.findById(1L)).thenReturn(Optional.of(kycDocument));
        when(kycDocumentRepository.save(any(KycDocument.class))).thenReturn(kycDocument);
        when(kycDocumentMapper.toKycDocumentDTO(any(KycDocument.class))).thenReturn(kycDocumentDTO);

        KycDocumentDTO result = kycDocumentService.updateDocument(1L, updateDTO);

        assertNotNull(result);
        verify(kycDocumentMapper).updateKycDocumentFromDto(updateDTO, kycDocument);
    }

    @Test
    void updateDocument_NotFound() {
        KycDocumentUpdateDTO updateDTO = new KycDocumentUpdateDTO();
        when(kycDocumentRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            kycDocumentService.updateDocument(1L, updateDTO));
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void verifyDocument_Success() {
        when(kycDocumentRepository.findById(1L)).thenReturn(Optional.of(kycDocument));
        when(userRepository.findById(2L)).thenReturn(Optional.of(verifier));
        when(kycDocumentRepository.save(any(KycDocument.class))).thenReturn(kycDocument);
        when(kycDocumentMapper.toKycDocumentDTO(any(KycDocument.class))).thenReturn(kycDocumentDTO);

        KycDocumentDTO result = kycDocumentService.verifyDocument(1L, 2L, "VERIFIED", "Document approved");

        assertNotNull(result);
        verify(kycDocumentRepository).save(kycDocument);
    }

    @Test
    void verifyDocument_DocumentNotFound() {
        when(kycDocumentRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            kycDocumentService.verifyDocument(1L, 2L, "VERIFIED", null));
        
        assertTrue(exception.getMessage().contains("KYC Document not found"));
    }

    @Test
    void verifyDocument_VerifierNotFound() {
        when(kycDocumentRepository.findById(1L)).thenReturn(Optional.of(kycDocument));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            kycDocumentService.verifyDocument(1L, 2L, "VERIFIED", null));
        
        assertTrue(exception.getMessage().contains("Verifier user not found"));
    }

    @Test
    void deleteDocument_Success() {
        when(kycDocumentRepository.existsById(1L)).thenReturn(true);

        kycDocumentService.deleteDocument(1L);

        verify(kycDocumentRepository).deleteById(1L);
    }

    @Test
    void deleteDocument_NotFound() {
        when(kycDocumentRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            kycDocumentService.deleteDocument(1L));
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void countVerifiedDocuments_Success() {
        when(kycDocumentRepository.countVerifiedDocumentsByOrganisation(1L)).thenReturn(5L);

        long count = kycDocumentService.countVerifiedDocuments(1L);

        assertEquals(5L, count);
    }
}
