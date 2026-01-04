package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.dto.OrganisationCreateDTO;
import com.fincore.usermgmt.dto.OrganisationDTO;
import com.fincore.usermgmt.dto.OrganisationUpdateDTO;
import com.fincore.usermgmt.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationMapperTest {

    private OrganisationMapper mapper;
    private AddressMapper addressMapper;

    @BeforeEach
    void setUp() {
        addressMapper = Mappers.getMapper(AddressMapper.class);
        // Create mapper instance and inject addressMapper using reflection
        mapper = new OrganisationMapperImpl();
        try {
            java.lang.reflect.Field field = mapper.getClass().getDeclaredField("addressMapper");
            field.setAccessible(true);
            field.set(mapper, addressMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject addressMapper", e);
        }
    }

    @Test
    void toOrganisationDTO_shouldMapAllFields() {
        // Given
        User owner = new User();
        owner.setId(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        Address registeredAddress = Address.builder()
                .id(1L)
                .typeCode(3)
                .addressLine1("123 Main St")
                .city("London")
                .country("UK")
                .build();

        Organisation organisation = Organisation.builder()
                .id(100L)
                .owner(owner)
                .registrationNumber("REG123")
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType(OrganisationType.LTD)
                .status(OrganisationStatus.ACTIVE)
                .registeredAddress(registeredAddress)
                .sicCode("12345")
                .countryOfIncorporation("UK")
                .incorporationDate(LocalDate.of(2020, 1, 1))
                .createdDatetime(LocalDateTime.now())
                .build();

        // When
        OrganisationDTO dto = mapper.toOrganisationDTO(organisation);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getOwnerId()).isEqualTo(1L);
        assertThat(dto.getOwnerName()).isEqualTo("John Doe");
        assertThat(dto.getRegistrationNumber()).isEqualTo("REG123");
        assertThat(dto.getLegalName()).isEqualTo("Test Company Ltd");
        assertThat(dto.getBusinessName()).isEqualTo("Test Business");
        assertThat(dto.getOrganisationType()).isEqualTo("LTD");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto.getSicCode()).isEqualTo("12345");
        assertThat(dto.getCountryOfIncorporation()).isEqualTo("UK");
    }

    @Test
    void toOrganisationDTO_withNullOwner_shouldReturnNull() {
        // Given
        Organisation organisation = Organisation.builder()
                .id(100L)
                .legalName("Test Company")
                .status(OrganisationStatus.PENDING)
                .build();

        // When
        OrganisationDTO dto = mapper.toOrganisationDTO(organisation);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getOwnerId()).isNull();
        assertThat(dto.getOwnerName()).isNull();
    }

    @Test
    void toOrganisationDTO_withOwnerMissingNames_shouldReturnEmptyString() {
        // Given
        User owner = new User();
        owner.setId(2L);

        Organisation organisation = Organisation.builder()
                .id(100L)
                .owner(owner)
                .legalName("Test Company")
                .build();

        // When
        OrganisationDTO dto = mapper.toOrganisationDTO(organisation);

        // Then
        assertThat(dto.getOwnerName()).isEmpty();
    }

    @Test
    void toOrganisation_shouldMapCreateDTO() {
        // Given
        OrganisationCreateDTO dto = OrganisationCreateDTO.builder()
                .registrationNumber("REG456")
                .legalName("New Company Ltd")
                .businessName("New Business")
                .organisationType("PLC")
                .sicCode("67890")
                .countryOfIncorporation("UK")
                .incorporationDate(LocalDate.of(2021, 6, 15))
                .build();

        // When
        Organisation organisation = mapper.toOrganisation(dto);

        // Then
        assertThat(organisation).isNotNull();
        assertThat(organisation.getId()).isNull();
        assertThat(organisation.getRegistrationNumber()).isEqualTo("REG456");
        assertThat(organisation.getLegalName()).isEqualTo("New Company Ltd");
        assertThat(organisation.getBusinessName()).isEqualTo("New Business");
        assertThat(organisation.getOrganisationType()).isEqualTo(OrganisationType.PLC);
        assertThat(organisation.getStatus()).isEqualTo(OrganisationStatus.PENDING);
        assertThat(organisation.getSicCode()).isEqualTo("67890");
    }

    @Test
    void updateOrganisationFromDto_shouldUpdateNonNullFields() {
        // Given
        Organisation existing = Organisation.builder()
                .id(100L)
                .legalName("Old Name")
                .businessName("Old Business")
                .sicCode("11111")
                .status(OrganisationStatus.PENDING)
                .build();

        OrganisationUpdateDTO updateDTO = OrganisationUpdateDTO.builder()
                .businessName("Updated Business")
                .sicCode("22222")
                .build();

        // When
        mapper.updateOrganisationFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getLegalName()).isEqualTo("Old Name"); // Not updated
        assertThat(existing.getBusinessName()).isEqualTo("Updated Business"); // Updated
        assertThat(existing.getSicCode()).isEqualTo("22222"); // Updated
    }

    @Test
    void getOwnerFullName_withBothNames_shouldConcatenate() {
        // Given
        User owner = new User();
        owner.setFirstName("Jane");
        owner.setLastName("Smith");

        Organisation organisation = Organisation.builder()
                .owner(owner)
                .build();

        // When
        String fullName = mapper.getOwnerFullName(organisation);

        // Then
        assertThat(fullName).isEqualTo("Jane Smith");
    }

    @Test
    void getOwnerFullName_withFirstNameOnly_shouldReturnFirstName() {
        // Given
        User owner = new User();
        owner.setFirstName("Jane");

        Organisation organisation = Organisation.builder()
                .owner(owner)
                .build();

        // When
        String fullName = mapper.getOwnerFullName(organisation);

        // Then
        assertThat(fullName).isEqualTo("Jane");
    }

    @Test
    void getOwnerFullName_withNullOwner_shouldReturnNull() {
        // Given
        Organisation organisation = Organisation.builder().build();

        // When
        String fullName = mapper.getOwnerFullName(organisation);

        // Then
        assertThat(fullName).isNull();
    }

    @Test
    void organisationTypeToString_withValidType_shouldReturnName() {
        // When
        String result = mapper.organisationTypeToString(OrganisationType.LTD);

        // Then
        assertThat(result).isEqualTo("LTD");
    }

    @Test
    void organisationTypeToString_withNull_shouldReturnNull() {
        // When
        String result = mapper.organisationTypeToString(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToOrganisationType_withValidString_shouldReturnEnum() {
        // When
        OrganisationType result = mapper.stringToOrganisationType("LTD");

        // Then
        assertThat(result).isEqualTo(OrganisationType.LTD);
    }

    @Test
    void stringToOrganisationType_withLowerCase_shouldReturnEnum() {
        // When
        OrganisationType result = mapper.stringToOrganisationType("plc");

        // Then
        assertThat(result).isEqualTo(OrganisationType.PLC);
    }

    @Test
    void stringToOrganisationType_withNull_shouldReturnNull() {
        // When
        OrganisationType result = mapper.stringToOrganisationType(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToOrganisationType_withEmpty_shouldReturnNull() {
        // When
        OrganisationType result = mapper.stringToOrganisationType("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToOrganisationType_withInvalidString_shouldReturnOther() {
        // When
        OrganisationType result = mapper.stringToOrganisationType("INVALID");

        // Then
        assertThat(result).isEqualTo(OrganisationType.OTHER);
    }

    @Test
    void statusToString_withValidStatus_shouldReturnName() {
        // When
        String result = mapper.statusToString(OrganisationStatus.ACTIVE);

        // Then
        assertThat(result).isEqualTo("ACTIVE");
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
        OrganisationStatus result = mapper.stringToStatus("PENDING");

        // Then
        assertThat(result).isEqualTo(OrganisationStatus.PENDING);
    }

    @Test
    void stringToStatus_withLowerCase_shouldReturnEnum() {
        // When
        OrganisationStatus result = mapper.stringToStatus("active");

        // Then
        assertThat(result).isEqualTo(OrganisationStatus.ACTIVE);
    }

    @Test
    void stringToStatus_withNull_shouldReturnNull() {
        // When
        OrganisationStatus result = mapper.stringToStatus(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToStatus_withEmpty_shouldReturnNull() {
        // When
        OrganisationStatus result = mapper.stringToStatus("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void stringToStatus_withInvalidString_shouldReturnNull() {
        // When
        OrganisationStatus result = mapper.stringToStatus("INVALID");

        // Then
        assertThat(result).isNull();
    }
}
