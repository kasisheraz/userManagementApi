package com.fincore.usermgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.KycDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KycDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KycDocumentService kycDocumentService;

    private KycDocumentDTO kycDocumentDTO;
    private KycDocumentCreateDTO createDTO;

    @BeforeEach
    void setUp() {
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
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createDocument_Success() throws Exception {
        when(kycDocumentService.createDocument(any(KycDocumentCreateDTO.class)))
                .thenReturn(kycDocumentDTO);

        mockMvc.perform(post("/api/kyc-documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentType").value("CERTIFICATE_OF_INCORPORATION"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getDocumentById_Found() throws Exception {
        when(kycDocumentService.getDocumentById(1L)).thenReturn(Optional.of(kycDocumentDTO));

        mockMvc.perform(get("/api/kyc-documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("certificate.pdf"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getDocumentById_NotFound() throws Exception {
        when(kycDocumentService.getDocumentById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/kyc-documents/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getDocumentsByOrganisation_Success() throws Exception {
        List<KycDocumentDTO> documents = Arrays.asList(kycDocumentDTO);
        when(kycDocumentService.getDocumentsByOrganisation(1L)).thenReturn(documents);

        mockMvc.perform(get("/api/kyc-documents/organisation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].organisationId").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getDocumentsByOrganisationPaged_Success() throws Exception {
        PagedResponse<KycDocumentDTO> pagedResponse = PagedResponse.<KycDocumentDTO>builder()
                .content(Arrays.asList(kycDocumentDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(kycDocumentService.getDocumentsByOrganisationPaged(eq(1L), anyInt(), anyInt()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/kyc-documents/organisation/1/paged")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getDocumentsByStatus_Success() throws Exception {
        List<KycDocumentDTO> documents = Arrays.asList(kycDocumentDTO);
        when(kycDocumentService.getDocumentsByStatus("PENDING")).thenReturn(documents);

        mockMvc.perform(get("/api/kyc-documents/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getPendingDocuments_Success() throws Exception {
        List<KycDocumentDTO> documents = Arrays.asList(kycDocumentDTO);
        when(kycDocumentService.getPendingDocuments()).thenReturn(documents);

        mockMvc.perform(get("/api/kyc-documents/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getDocumentsByOrganisationAndType_Success() throws Exception {
        List<KycDocumentDTO> documents = Arrays.asList(kycDocumentDTO);
        when(kycDocumentService.getDocumentsByOrganisationAndType(1L, "CERTIFICATE_OF_INCORPORATION"))
                .thenReturn(documents);

        mockMvc.perform(get("/api/kyc-documents/organisation/1/type/CERTIFICATE_OF_INCORPORATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentType").value("CERTIFICATE_OF_INCORPORATION"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateDocument_Success() throws Exception {
        KycDocumentUpdateDTO updateDTO = KycDocumentUpdateDTO.builder()
                .fileName("updated_certificate.pdf")
                .build();

        when(kycDocumentService.updateDocument(eq(1L), any(KycDocumentUpdateDTO.class)))
                .thenReturn(kycDocumentDTO);

        mockMvc.perform(put("/api/kyc-documents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void verifyDocument_Success() throws Exception {
        kycDocumentDTO.setStatus("VERIFIED");
        when(kycDocumentService.verifyDocument(eq(1L), eq(2L), eq("VERIFIED"), anyString()))
                .thenReturn(kycDocumentDTO);

        mockMvc.perform(post("/api/kyc-documents/1/verify")
                        .param("verifierId", "2")
                        .param("status", "VERIFIED")
                        .param("reason", "Document approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteDocument_Success() throws Exception {
        mockMvc.perform(delete("/api/kyc-documents/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void countVerifiedDocuments_Success() throws Exception {
        when(kycDocumentService.countVerifiedDocuments(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/kyc-documents/organisation/1/verified/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void createDocument_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/kyc-documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }
}
