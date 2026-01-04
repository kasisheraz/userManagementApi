package com.fincore.usermgmt.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Organisation entity lifecycle methods and JPA behavior.
 * Tests @PrePersist, @PreUpdate callbacks, entity relationships, and cascading.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Organisation Entity Tests")
class OrganisationEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    private User testOwner;

    @BeforeEach
    void setUp() {
        // Create test role and owner
        Role role = new Role();
        role.setName("ADMIN");
        role.setDescription("Admin role");
        entityManager.persist(role);

        testOwner = new User();
        testOwner.setPhoneNumber("+1234567890");
        testOwner.setFirstName("Owner");
        testOwner.setLastName("User");
        testOwner.setRole(role);
        entityManager.persist(testOwner);
        entityManager.flush();
    }

    @Test
    @DisplayName("@PrePersist should set timestamps and default status on new organisation")
    void prePersist_shouldSetTimestampsAndDefaultStatus() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();

        // Act
        LocalDateTime beforePersist = LocalDateTime.now().minusSeconds(1);
        Organisation savedOrg = entityManager.persistAndFlush(org);
        LocalDateTime afterPersist = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(savedOrg.getCreatedDatetime()).isNotNull();
        assertThat(savedOrg.getLastModifiedDatetime()).isNotNull();
        assertThat(savedOrg.getCreatedDatetime()).isBetween(beforePersist, afterPersist);
        assertThat(savedOrg.getLastModifiedDatetime()).isBetween(beforePersist, afterPersist);
        assertThat(savedOrg.getStatus()).isEqualTo(OrganisationStatus.PENDING);
    }

    @Test
    @DisplayName("@PreUpdate should update lastModifiedDatetime on organisation update")
    void preUpdate_shouldUpdateLastModifiedDatetime() throws InterruptedException {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();
        Organisation savedOrg = entityManager.persistAndFlush(org);
        
        LocalDateTime originalCreated = savedOrg.getCreatedDatetime();
        LocalDateTime originalModified = savedOrg.getLastModifiedDatetime();
        
        Thread.sleep(100);

        // Act
        savedOrg.setBusinessName("Updated Business Name");
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        entityManager.persistAndFlush(savedOrg);
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(savedOrg.getCreatedDatetime()).isEqualTo(originalCreated);
        assertThat(savedOrg.getLastModifiedDatetime()).isNotEqualTo(originalModified);
        assertThat(savedOrg.getLastModifiedDatetime()).isBetween(beforeUpdate, afterUpdate);
    }

    @Test
    @DisplayName("Organisation should persist with all required fields")
    void persist_withRequiredFields_shouldSucceed() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);
        entityManager.clear();
        Organisation retrieved = entityManager.find(Organisation.class, savedOrg.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getLegalName()).isEqualTo("Test Company Ltd");
        assertThat(retrieved.getOrganisationType()).isEqualTo(OrganisationType.LTD);
        assertThat(retrieved.getOwner().getId()).isEqualTo(testOwner.getId());
    }

    @Test
    @DisplayName("Organisation should persist with comprehensive business details")
    void persist_withComprehensiveDetails_shouldSucceed() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Acme Corporation Ltd")
                .businessName("Acme Corp")
                .organisationType(OrganisationType.LTD)
                .registrationNumber("12345678")
                .companyNumber("AB123456")
                .sicCode("62011")
                .businessDescription("Software development and consulting")
                .incorporationDate(LocalDate.of(2020, 1, 15))
                .countryOfIncorporation("United Kingdom")
                .typeOfBusinessCode("IT_SERVICES")
                .numberOfBranches("5")
                .numberOfAgents("10")
                .hmrcMlrNumber("XDML00000123456")
                .hmrcExpiryDate(LocalDate.now().plusYears(1))
                .fcaNumber("FCA123456")
                .icoNumber("ICO123456")
                .mlroDetails("John Smith - MLRO")
                .status(OrganisationStatus.ACTIVE)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);
        entityManager.clear();
        Organisation retrieved = entityManager.find(Organisation.class, savedOrg.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getLegalName()).isEqualTo("Acme Corporation Ltd");
        assertThat(retrieved.getBusinessName()).isEqualTo("Acme Corp");
        assertThat(retrieved.getRegistrationNumber()).isEqualTo("12345678");
        assertThat(retrieved.getCompanyNumber()).isEqualTo("AB123456");
        assertThat(retrieved.getSicCode()).isEqualTo("62011");
        assertThat(retrieved.getBusinessDescription()).isEqualTo("Software development and consulting");
        assertThat(retrieved.getIncorporationDate()).isEqualTo(LocalDate.of(2020, 1, 15));
        assertThat(retrieved.getCountryOfIncorporation()).isEqualTo("United Kingdom");
        assertThat(retrieved.getNumberOfBranches()).isEqualTo("5");
        assertThat(retrieved.getNumberOfAgents()).isEqualTo("10");
        assertThat(retrieved.getHmrcMlrNumber()).isEqualTo("XDML00000123456");
        assertThat(retrieved.getFcaNumber()).isEqualTo("FCA123456");
        assertThat(retrieved.getIcoNumber()).isEqualTo("ICO123456");
        assertThat(retrieved.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
    }

    @Test
    @DisplayName("Organisation should maintain relationship with owner User")
    void persist_shouldMaintainOwnerRelationship() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);
        entityManager.clear();
        Organisation retrieved = entityManager.find(Organisation.class, savedOrg.getId());

        // Assert
        assertThat(retrieved.getOwner()).isNotNull();
        assertThat(retrieved.getOwner().getId()).isEqualTo(testOwner.getId());
        assertThat(retrieved.getOwner().getFirstName()).isEqualTo("Owner");
    }

    @Test
    @DisplayName("Organisation status should default to PENDING if not set")
    void persist_withoutStatus_shouldDefaultToPending() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);

        // Assert
        assertThat(savedOrg.getStatus()).isEqualTo(OrganisationStatus.PENDING);
    }

    @Test
    @DisplayName("Organisation status should preserve explicit value")
    void persist_withExplicitStatus_shouldPreserveStatus() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .status(OrganisationStatus.ACTIVE)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);

        // Assert
        assertThat(savedOrg.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
    }

    @Test
    @DisplayName("Organisation can be updated with new status")
    void update_shouldChangeStatus() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();
        Organisation savedOrg = entityManager.persistAndFlush(org);

        // Act
        savedOrg.setStatus(OrganisationStatus.ACTIVE);
        entityManager.persistAndFlush(savedOrg);
        entityManager.clear();
        Organisation updated = entityManager.find(Organisation.class, savedOrg.getId());

        // Assert
        assertThat(updated.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
    }

    @Test
    @DisplayName("Organisation can be updated with regulatory information")
    void update_shouldChangeRegulatoryInfo() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();
        Organisation savedOrg = entityManager.persistAndFlush(org);

        // Act
        savedOrg.setHmrcMlrNumber("XDML00000123456");
        savedOrg.setFcaNumber("FCA987654");
        savedOrg.setIcoNumber("ICO567890");
        entityManager.persistAndFlush(savedOrg);
        entityManager.clear();
        Organisation updated = entityManager.find(Organisation.class, savedOrg.getId());

        // Assert
        assertThat(updated.getHmrcMlrNumber()).isEqualTo("XDML00000123456");
        assertThat(updated.getFcaNumber()).isEqualTo("FCA987654");
        assertThat(updated.getIcoNumber()).isEqualTo("ICO567890");
    }

    @Test
    @DisplayName("Multiple organisations can have the same owner")
    void persist_multipleOrganisationsWithSameOwner_shouldSucceed() {
        // Arrange
        Organisation org1 = Organisation.builder()
                .owner(testOwner)
                .legalName("Company One Ltd")
                .organisationType(OrganisationType.LTD)
                .build();

        Organisation org2 = Organisation.builder()
                .owner(testOwner)
                .legalName("Company Two Ltd")
                .organisationType(OrganisationType.SOLE_TRADER)
                .build();

        // Act
        Organisation saved1 = entityManager.persistAndFlush(org1);
        Organisation saved2 = entityManager.persistAndFlush(org2);

        // Assert
        assertThat(saved1.getOwner().getId()).isEqualTo(saved2.getOwner().getId());
    }

    @Test
    @DisplayName("Organisation can persist with Address cascade")
    void persist_withAddressCascade_shouldSucceed() {
        // Arrange
        Address businessAddress = Address.builder()
                .typeCode(1)
                .addressLine1("123 Business St")
                .country("UK")
                .build();

        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .businessAddress(businessAddress)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);
        entityManager.clear();
        Organisation retrieved = entityManager.find(Organisation.class, savedOrg.getId());

        // Assert
        assertThat(retrieved.getBusinessAddress()).isNotNull();
        assertThat(retrieved.getBusinessAddress().getAddressLine1()).isEqualTo("123 Business St");
    }

    @Test
    @DisplayName("Organisation timestamps should not be null after persistence")
    void persist_timestampsShouldNeverBeNull() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);

        // Assert
        assertThat(savedOrg.getCreatedDatetime()).as("Created datetime should not be null").isNotNull();
        assertThat(savedOrg.getLastModifiedDatetime()).as("Last modified datetime should not be null").isNotNull();
    }

    @Test
    @DisplayName("Organisation can be created with different organisation types")
    void persist_withDifferentTypes_shouldSucceed() {
        // Arrange & Act
        Organisation limited = Organisation.builder()
                .owner(testOwner)
                .legalName("Limited Company")
                .organisationType(OrganisationType.LTD)
                .build();
        Organisation savedLimited = entityManager.persistAndFlush(limited);

        Organisation partnership = Organisation.builder()
                .owner(testOwner)
                .legalName("Partnership Firm")
                .organisationType(OrganisationType.PARTNERSHIP)
                .build();
        Organisation savedPartnership = entityManager.persistAndFlush(partnership);

        Organisation soleTrader = Organisation.builder()
                .owner(testOwner)
                .legalName("Sole Trader Business")
                .organisationType(OrganisationType.SOLE_TRADER)
                .build();
        Organisation savedSoleTrader = entityManager.persistAndFlush(soleTrader);

        // Assert
        assertThat(savedLimited.getOrganisationType()).isEqualTo(OrganisationType.LTD);
        assertThat(savedPartnership.getOrganisationType()).isEqualTo(OrganisationType.PARTNERSHIP);
        assertThat(savedSoleTrader.getOrganisationType()).isEqualTo(OrganisationType.SOLE_TRADER);
    }

    @Test
    @DisplayName("Organisation can handle null optional fields")
    void persist_withNullOptionalFields_shouldSucceed() {
        // Arrange
        Organisation org = Organisation.builder()
                .owner(testOwner)
                .legalName("Test Company Ltd")
                .organisationType(OrganisationType.LTD)
                .businessName(null)
                .registrationNumber(null)
                .sicCode(null)
                .build();

        // Act
        Organisation savedOrg = entityManager.persistAndFlush(org);
        entityManager.clear();
        Organisation retrieved = entityManager.find(Organisation.class, savedOrg.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getBusinessName()).isNull();
        assertThat(retrieved.getRegistrationNumber()).isNull();
        assertThat(retrieved.getSicCode()).isNull();
    }
}
