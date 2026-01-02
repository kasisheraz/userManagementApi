package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.entity.*;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.mapper.OrganisationMapper;
import com.fincore.usermgmt.repository.AddressRepository;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganisationMapper organisationMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private OrganisationService organisationService;

    private User owner;
    private Organisation organisation;
    private OrganisationDTO organisationDTO;
    private OrganisationCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setEmail("john@example.com");

        organisation = Organisation.builder()
                .id(1L)
                .owner(owner)
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType(OrganisationType.LTD)
                .status(OrganisationStatus.PENDING)
                .registrationNumber("12345678")
                .companyNumber("CN12345")
                .countryOfIncorporation("United Kingdom")
                .build();

        organisationDTO = OrganisationDTO.builder()
                .id(1L)
                .ownerId(1L)
                .ownerName("John Doe")
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType("LTD")
                .status("PENDING")
                .registrationNumber("12345678")
                .companyNumber("CN12345")
                .countryOfIncorporation("United Kingdom")
                .build();

        createDTO = OrganisationCreateDTO.builder()
                .ownerId(1L)
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType("LTD")
                .registrationNumber("12345678")
                .companyNumber("CN12345")
                .countryOfIncorporation("United Kingdom")
                .build();
    }

    @Test
    void createOrganisation_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(organisationRepository.existsByRegistrationNumber("12345678")).thenReturn(false);
        when(organisationRepository.existsByCompanyNumber("CN12345")).thenReturn(false);
        when(organisationMapper.toOrganisation(any(OrganisationCreateDTO.class))).thenReturn(organisation);
        when(organisationRepository.save(any(Organisation.class))).thenReturn(organisation);
        when(organisationMapper.toOrganisationDTO(any(Organisation.class))).thenReturn(organisationDTO);

        OrganisationDTO result = organisationService.createOrganisation(createDTO);

        assertNotNull(result);
        assertEquals("Test Company Ltd", result.getLegalName());
        assertEquals(1L, result.getOwnerId());
        verify(organisationRepository).save(any(Organisation.class));
    }

    @Test
    void createOrganisation_OwnerNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            organisationService.createOrganisation(createDTO));
        
        assertTrue(exception.getMessage().contains("Owner user not found"));
    }

    @Test
    void createOrganisation_DuplicateRegistrationNumber() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(organisationRepository.existsByRegistrationNumber("12345678")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            organisationService.createOrganisation(createDTO));
        
        assertTrue(exception.getMessage().contains("registration number already exists"));
    }

    @Test
    void getOrganisationById_Found() {
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(organisation));
        when(organisationMapper.toOrganisationDTO(organisation)).thenReturn(organisationDTO);

        Optional<OrganisationDTO> result = organisationService.getOrganisationById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Company Ltd", result.get().getLegalName());
    }

    @Test
    void getOrganisationById_NotFound() {
        when(organisationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<OrganisationDTO> result = organisationService.getOrganisationById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllOrganisations_Success() {
        Page<Organisation> page = new PageImpl<>(Arrays.asList(organisation));
        when(organisationRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(organisationMapper.toOrganisationDTO(organisation)).thenReturn(organisationDTO);

        PagedResponse<OrganisationDTO> result = organisationService.getAllOrganisations(0, 20, "legalName", "ASC");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrganisationsByOwner_Success() {
        when(organisationRepository.findByOwnerId(1L)).thenReturn(Arrays.asList(organisation));
        when(organisationMapper.toOrganisationDTO(organisation)).thenReturn(organisationDTO);

        List<OrganisationDTO> result = organisationService.getOrganisationsByOwner(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Company Ltd", result.get(0).getLegalName());
    }

    @Test
    void getOrganisationsByStatus_Success() {
        when(organisationRepository.findByStatus(OrganisationStatus.PENDING))
                .thenReturn(Arrays.asList(organisation));
        when(organisationMapper.toOrganisationDTO(organisation)).thenReturn(organisationDTO);

        List<OrganisationDTO> result = organisationService.getOrganisationsByStatus("PENDING");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateOrganisation_Success() {
        OrganisationUpdateDTO updateDTO = OrganisationUpdateDTO.builder()
                .businessName("Updated Business Name")
                .build();

        when(organisationRepository.findById(1L)).thenReturn(Optional.of(organisation));
        when(organisationRepository.save(any(Organisation.class))).thenReturn(organisation);
        when(organisationMapper.toOrganisationDTO(any(Organisation.class))).thenReturn(organisationDTO);

        OrganisationDTO result = organisationService.updateOrganisation(1L, updateDTO);

        assertNotNull(result);
        verify(organisationMapper).updateOrganisationFromDto(updateDTO, organisation);
    }

    @Test
    void updateOrganisation_NotFound() {
        OrganisationUpdateDTO updateDTO = new OrganisationUpdateDTO();
        when(organisationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            organisationService.updateOrganisation(1L, updateDTO));
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void updateOrganisationStatus_Success() {
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(organisation));
        when(organisationRepository.save(any(Organisation.class))).thenReturn(organisation);
        when(organisationMapper.toOrganisationDTO(any(Organisation.class))).thenReturn(organisationDTO);

        OrganisationDTO result = organisationService.updateOrganisationStatus(1L, "ACTIVE", "Approved");

        assertNotNull(result);
        verify(organisationRepository).save(organisation);
    }

    @Test
    void deleteOrganisation_Success() {
        when(organisationRepository.existsById(1L)).thenReturn(true);

        organisationService.deleteOrganisation(1L);

        verify(organisationRepository).deleteById(1L);
    }

    @Test
    void deleteOrganisation_NotFound() {
        when(organisationRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            organisationService.deleteOrganisation(1L));
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void searchOrganisations_Success() {
        OrganisationSearchDTO searchDTO = OrganisationSearchDTO.builder()
                .searchTerm("Test")
                .status("PENDING")
                .page(0)
                .size(20)
                .sortBy("legalName")
                .sortDirection("ASC")
                .build();

        Page<Organisation> page = new PageImpl<>(Arrays.asList(organisation));
        when(organisationRepository.searchOrganisations(eq("Test"), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(organisationMapper.toOrganisationDTO(organisation)).thenReturn(organisationDTO);

        PagedResponse<OrganisationDTO> result = organisationService.searchOrganisations(searchDTO);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
