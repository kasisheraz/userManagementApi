package com.fincore.usermgmt.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for Address entity lifecycle methods and JPA behavior.
 * Tests @PrePersist callback and entity state management.
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Address Entity Tests")
class AddressEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("@PrePersist should set createdDatetime and default status on new address")
    void prePersist_shouldSetTimestampAndDefaultStatus() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();

        // Act
        LocalDateTime beforePersist = LocalDateTime.now().minusSeconds(1);
        Address savedAddress = entityManager.persistAndFlush(address);
        LocalDateTime afterPersist = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(savedAddress.getCreatedDatetime()).isNotNull();
        assertThat(savedAddress.getCreatedDatetime()).isBetween(beforePersist, afterPersist);
        assertThat(savedAddress.getStatusDescription()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Address should persist with all required fields")
    void persist_withRequiredFields_shouldSucceed() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("456 Business Road")
                .country("United States")
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);
        entityManager.clear();
        Address retrieved = entityManager.find(Address.class, savedAddress.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTypeCode()).isEqualTo(1);
        assertThat(retrieved.getAddressLine1()).isEqualTo("456 Business Road");
        assertThat(retrieved.getCountry()).isEqualTo("United States");
    }

    @Test
    @DisplayName("Address should persist with comprehensive details")
    void persist_withComprehensiveDetails_shouldSucceed() {
        // Arrange
        Address address = Address.builder()
                .typeCode(2)
                .addressLine1("789 Park Avenue")
                .addressLine2("Suite 100")
                .postalCode("SW1A 1AA")
                .stateCode("LON")
                .city("London")
                .country("United Kingdom")
                .statusDescription("VERIFIED")
                .createdBy(1L)
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);
        entityManager.clear();
        Address retrieved = entityManager.find(Address.class, savedAddress.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTypeCode()).isEqualTo(2);
        assertThat(retrieved.getAddressLine1()).isEqualTo("789 Park Avenue");
        assertThat(retrieved.getAddressLine2()).isEqualTo("Suite 100");
        assertThat(retrieved.getPostalCode()).isEqualTo("SW1A 1AA");
        assertThat(retrieved.getStateCode()).isEqualTo("LON");
        assertThat(retrieved.getCity()).isEqualTo("London");
        assertThat(retrieved.getCountry()).isEqualTo("United Kingdom");
        assertThat(retrieved.getStatusDescription()).isEqualTo("VERIFIED");
        assertThat(retrieved.getCreatedBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Address status should default to ACTIVE if not set")
    void persist_withoutStatus_shouldDefaultToActive() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);

        // Assert
        assertThat(savedAddress.getStatusDescription()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Address status should preserve explicit value")
    void persist_withExplicitStatus_shouldPreserveStatus() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .statusDescription("INACTIVE")
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);

        // Assert
        assertThat(savedAddress.getStatusDescription()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("Address can be updated with new details")
    void update_shouldChangeAddressDetails() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();
        Address savedAddress = entityManager.persistAndFlush(address);

        // Act
        savedAddress.setAddressLine1("456 Updated Street");
        savedAddress.setAddressLine2("Apartment 5B");
        savedAddress.setCity("Manchester");
        entityManager.persistAndFlush(savedAddress);
        entityManager.clear();
        Address updated = entityManager.find(Address.class, savedAddress.getId());

        // Assert
        assertThat(updated.getAddressLine1()).isEqualTo("456 Updated Street");
        assertThat(updated.getAddressLine2()).isEqualTo("Apartment 5B");
        assertThat(updated.getCity()).isEqualTo("Manchester");
    }

    @Test
    @DisplayName("Address can be updated with new status")
    void update_shouldChangeStatus() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();
        Address savedAddress = entityManager.persistAndFlush(address);

        // Act
        savedAddress.setStatusDescription("INACTIVE");
        entityManager.persistAndFlush(savedAddress);
        entityManager.clear();
        Address updated = entityManager.find(Address.class, savedAddress.getId());

        // Assert
        assertThat(updated.getStatusDescription()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("Address getAddressType should return correct enum from typeCode")
    void getAddressType_shouldReturnCorrectEnum() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();
        Address savedAddress = entityManager.persistAndFlush(address);

        // Act
        AddressType addressType = savedAddress.getAddressType();

        // Assert
        assertThat(addressType).isNotNull();
        assertThat(addressType.getCode()).isEqualTo(1);
    }

    @Test
    @DisplayName("Address setAddressType should set correct typeCode from enum")
    void setAddressType_shouldSetCorrectTypeCode() {
        // Arrange
        Address address = Address.builder()
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();

        // Act
        address.setAddressType(AddressType.RESIDENTIAL);
        Address savedAddress = entityManager.persistAndFlush(address);

        // Assert
        assertThat(savedAddress.getTypeCode()).isEqualTo(AddressType.RESIDENTIAL.getCode());
    }

    @Test
    @DisplayName("Address can handle null optional fields")
    void persist_withNullOptionalFields_shouldSucceed() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .addressLine2(null)
                .postalCode(null)
                .stateCode(null)
                .city(null)
                .country("United Kingdom")
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);
        entityManager.clear();
        Address retrieved = entityManager.find(Address.class, savedAddress.getId());

        // Assert
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getAddressLine2()).isNull();
        assertThat(retrieved.getPostalCode()).isNull();
        assertThat(retrieved.getStateCode()).isNull();
        assertThat(retrieved.getCity()).isNull();
    }

    @Test
    @DisplayName("Address createdDatetime should not be null after persistence")
    void persist_createdDatetimeShouldNeverBeNull() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);

        // Assert
        assertThat(savedAddress.getCreatedDatetime()).as("Created datetime should not be null").isNotNull();
    }

    @Test
    @DisplayName("Multiple addresses with same details can be persisted")
    void persist_multipleAddressesWithSameDetails_shouldSucceed() {
        // Arrange
        Address address1 = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();

        Address address2 = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .country("United Kingdom")
                .build();

        // Act
        Address saved1 = entityManager.persistAndFlush(address1);
        Address saved2 = entityManager.persistAndFlush(address2);

        // Assert
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
        assertThat(saved1.getAddressLine1()).isEqualTo(saved2.getAddressLine1());
    }

    @Test
    @DisplayName("Address can be created with different type codes")
    void persist_withDifferentTypeCodes_shouldSucceed() {
        // Arrange & Act
        Address residential = Address.builder()
                .typeCode(1)
                .addressLine1("123 Residential St")
                .country("UK")
                .build();
        Address savedResidential = entityManager.persistAndFlush(residential);

        Address business = Address.builder()
                .typeCode(2)
                .addressLine1("456 Business Ave")
                .country("UK")
                .build();
        Address savedBusiness = entityManager.persistAndFlush(business);

        Address postal = Address.builder()
                .typeCode(3)
                .addressLine1("789 Postal Rd")
                .country("UK")
                .build();
        Address savedPostal = entityManager.persistAndFlush(postal);

        // Assert
        assertThat(savedResidential.getTypeCode()).isEqualTo(1);
        assertThat(savedBusiness.getTypeCode()).isEqualTo(2);
        assertThat(savedPostal.getTypeCode()).isEqualTo(3);
    }

    @Test
    @DisplayName("Address with special characters in fields should persist")
    void persist_withSpecialCharacters_shouldSucceed() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("Flat 3B, O'Connor's Building")
                .addressLine2("St. Mary's Street")
                .postalCode("SW1A-1AA")
                .city("London")
                .country("United Kingdom")
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);
        entityManager.clear();
        Address retrieved = entityManager.find(Address.class, savedAddress.getId());

        // Assert
        assertThat(retrieved.getAddressLine1()).isEqualTo("Flat 3B, O'Connor's Building");
        assertThat(retrieved.getAddressLine2()).isEqualTo("St. Mary's Street");
    }

    @Test
    @DisplayName("Address with long postal code should persist")
    void persist_withLongPostalCode_shouldSucceed() {
        // Arrange
        Address address = Address.builder()
                .typeCode(1)
                .addressLine1("123 Main Street")
                .postalCode("12345-6789")
                .country("United States")
                .build();

        // Act
        Address savedAddress = entityManager.persistAndFlush(address);
        entityManager.clear();
        Address retrieved = entityManager.find(Address.class, savedAddress.getId());

        // Assert
        assertThat(retrieved.getPostalCode()).isEqualTo("12345-6789");
    }
}
