package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrganisationRepositoryTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User owner;
    private Organisation organisation;

    @BeforeEach
    void setUp() {
        // Create role
        Role role = new Role();
        role.setName("TEST_ROLE");
        role.setDescription("Test Role for Repository Tests");
        role = roleRepository.save(role);

        // Create owner user with unique phone number
        owner = new User();
        owner.setPhoneNumber("+9999990001");
        owner.setEmail("owner.repo.test@test.com");
        owner.setFirstName("Test");
        owner.setLastName("Owner");
        owner.setRole(role);
        owner.setStatusDescription("ACTIVE");
        owner = userRepository.save(owner);

        // Create organisation
        organisation = Organisation.builder()
                .owner(owner)
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType(OrganisationType.LTD)
                .status(OrganisationStatus.PENDING)
                .registrationNumber("12345678")
                .companyNumber("CN12345")
                .countryOfIncorporation("United Kingdom")
                .incorporationDate(LocalDate.of(2020, 1, 15))
                .build();
        organisation = organisationRepository.save(organisation);
    }

    @Test
    void findByRegistrationNumber_Found() {
        Optional<Organisation> result = organisationRepository.findByRegistrationNumber("12345678");
        
        assertTrue(result.isPresent());
        assertEquals("Test Company Ltd", result.get().getLegalName());
    }

    @Test
    void findByRegistrationNumber_NotFound() {
        Optional<Organisation> result = organisationRepository.findByRegistrationNumber("99999999");
        
        assertFalse(result.isPresent());
    }

    @Test
    void findByCompanyNumber_Found() {
        Optional<Organisation> result = organisationRepository.findByCompanyNumber("CN12345");
        
        assertTrue(result.isPresent());
        assertEquals("Test Company Ltd", result.get().getLegalName());
    }

    @Test
    void findByLegalName_Found() {
        Optional<Organisation> result = organisationRepository.findByLegalName("Test Company Ltd");
        
        assertTrue(result.isPresent());
        assertEquals("12345678", result.get().getRegistrationNumber());
    }

    @Test
    void findByOwnerId_Found() {
        List<Organisation> result = organisationRepository.findByOwnerId(owner.getId());
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Company Ltd", result.get(0).getLegalName());
    }

    @Test
    void findByStatus_Found() {
        List<Organisation> result = organisationRepository.findByStatus(OrganisationStatus.PENDING);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByOrganisationType_Found() {
        List<Organisation> result = organisationRepository.findByOrganisationType(OrganisationType.LTD);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByCountryOfIncorporation_Found() {
        List<Organisation> result = organisationRepository.findByCountryOfIncorporation("United Kingdom");
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByLegalNameContainingIgnoreCase_Found() {
        List<Organisation> result = organisationRepository.findByLegalNameContainingIgnoreCase("test");
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void existsByRegistrationNumber_True() {
        assertTrue(organisationRepository.existsByRegistrationNumber("12345678"));
    }

    @Test
    void existsByRegistrationNumber_False() {
        assertFalse(organisationRepository.existsByRegistrationNumber("99999999"));
    }

    @Test
    void existsByCompanyNumber_True() {
        assertTrue(organisationRepository.existsByCompanyNumber("CN12345"));
    }

    @Test
    void existsByCompanyNumber_False() {
        assertFalse(organisationRepository.existsByCompanyNumber("CN99999"));
    }

    @Test
    void findAllPaged_Success() {
        Page<Organisation> result = organisationRepository.findAll(PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByStatusPaged_Success() {
        Page<Organisation> result = organisationRepository.findByStatus(
                OrganisationStatus.PENDING, PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchOrganisations_ByName() {
        Page<Organisation> result = organisationRepository.searchOrganisations(
                "Test", null, null, PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchOrganisations_ByStatus() {
        Page<Organisation> result = organisationRepository.searchOrganisations(
                null, OrganisationStatus.PENDING, null, PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchOrganisations_ByType() {
        Page<Organisation> result = organisationRepository.searchOrganisations(
                null, null, OrganisationType.LTD, PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchOrganisations_AllFilters() {
        Page<Organisation> result = organisationRepository.searchOrganisations(
                "Test", OrganisationStatus.PENDING, OrganisationType.LTD, PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchOrganisations_NoMatch() {
        Page<Organisation> result = organisationRepository.searchOrganisations(
                "NonExistent", null, null, PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }
}
