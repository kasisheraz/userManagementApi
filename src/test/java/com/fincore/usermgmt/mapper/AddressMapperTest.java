package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.entity.Address;
import com.fincore.usermgmt.entity.AddressType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AddressMapperTest {

    private AddressMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AddressMapper.class);
    }

    @Test
    void toAddressDTO_shouldMapAllFields() {
        // Given
        Address address = Address.builder()
                .id(1L)
                .typeCode(3) // REGISTERED
                .addressLine1("123 Main Street")
                .addressLine2("Floor 5")
                .postalCode("EC1A 1BB")
                .stateCode("Greater London")
                .city("London")
                .country("United Kingdom")
                .statusDescription("ACTIVE")
                .createdDatetime(LocalDateTime.now())
                .createdBy(1L)
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAddressType()).isEqualTo("REGISTERED");
        assertThat(dto.getAddressLine1()).isEqualTo("123 Main Street");
        assertThat(dto.getAddressLine2()).isEqualTo("Floor 5");
        assertThat(dto.getPostalCode()).isEqualTo("EC1A 1BB");
        assertThat(dto.getStateCode()).isEqualTo("Greater London");
        assertThat(dto.getCity()).isEqualTo("London");
        assertThat(dto.getCountry()).isEqualTo("United Kingdom");
        assertThat(dto.getStatusDescription()).isEqualTo("ACTIVE");
    }

    @Test
    void toAddressDTO_withBusinessType_shouldMapCorrectly() {
        // Given
        Address address = Address.builder()
                .id(2L)
                .typeCode(2) // BUSINESS
                .addressLine1("456 Business Park")
                .city("Manchester")
                .country("UK")
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto.getAddressType()).isEqualTo("BUSINESS");
    }

    @Test
    void toAddressDTO_withResidentialType_shouldMapCorrectly() {
        // Given
        Address address = Address.builder()
                .id(3L)
                .typeCode(1) // RESIDENTIAL
                .addressLine1("789 Home Street")
                .city("Birmingham")
                .country("UK")
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto.getAddressType()).isEqualTo("RESIDENTIAL");
    }

    @Test
    void toAddressDTO_withCorrespondenceType_shouldMapCorrectly() {
        // Given
        Address address = Address.builder()
                .id(4L)
                .typeCode(4) // CORRESPONDENCE
                .addressLine1("101 Mail Street")
                .city("Leeds")
                .country("UK")
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto.getAddressType()).isEqualTo("CORRESPONDENCE");
    }

    @Test
    void toAddressDTO_withOtherType_shouldMapCorrectly() {
        // Given
        Address address = Address.builder()
                .id(5L)
                .typeCode(5) // POSTAL
                .addressLine1("202 Postal Street")
                .city("Liverpool")
                .country("UK")
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto.getAddressType()).isEqualTo("POSTAL");
    }

    @Test
    void toAddressDTO_withNullTypeCode_shouldReturnNull() {
        // Given
        Address address = Address.builder()
                .id(6L)
                .addressLine1("No Type Street")
                .city("Glasgow")
                .country("UK")
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto.getAddressType()).isNull();
    }

    @Test
    void toAddressDTO_withInvalidTypeCode_shouldReturnUnknown() {
        // Given
        Address address = Address.builder()
                .id(7L)
                .typeCode(999) // Invalid code
                .addressLine1("Invalid Type Street")
                .city("Edinburgh")
                .country("UK")
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto.getAddressType()).isEqualTo("UNKNOWN");
    }

    @Test
    void toAddress_shouldMapCreateDTO() {
        // Given
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(3)
                .addressLine1("New Address Street")
                .addressLine2("Suite 100")
                .postalCode("M1 2AB")
                .stateCode("Greater Manchester")
                .city("Manchester")
                .country("United Kingdom")
                .build();

        // When
        Address address = mapper.toAddress(dto);

        // Then
        assertThat(address).isNotNull();
        assertThat(address.getId()).isNull();
        assertThat(address.getTypeCode()).isEqualTo(3);
        assertThat(address.getAddressLine1()).isEqualTo("New Address Street");
        assertThat(address.getAddressLine2()).isEqualTo("Suite 100");
        assertThat(address.getPostalCode()).isEqualTo("M1 2AB");
        assertThat(address.getStateCode()).isEqualTo("Greater Manchester");
        assertThat(address.getCity()).isEqualTo("Manchester");
        assertThat(address.getCountry()).isEqualTo("United Kingdom");
        // statusDescription is set automatically by @PrePersist to "ACTIVE"
    }

    @Test
    void updateAddressFromDto_shouldUpdateNonNullFields() {
        // Given
        Address existing = Address.builder()
                .id(1L)
                .typeCode(3)
                .addressLine1("Old Street")
                .addressLine2("Old Floor")
                .postalCode("OLD 123")
                .city("OldCity")
                .country("OldCountry")
                .statusDescription("ACTIVE")
                .build();

        AddressCreateDTO updateDTO = AddressCreateDTO.builder()
                .typeCode(3)
                .addressLine1("Updated Street")
                .addressLine2("Updated Floor")
                .postalCode("NEW 456")
                .city("NewCity")
                .country("NewCountry")
                .build();

        // When
        mapper.updateAddressFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getAddressLine1()).isEqualTo("Updated Street");
        assertThat(existing.getAddressLine2()).isEqualTo("Updated Floor");
        assertThat(existing.getPostalCode()).isEqualTo("NEW 456");
        assertThat(existing.getCity()).isEqualTo("NewCity");
        assertThat(existing.getCountry()).isEqualTo("NewCountry");
        assertThat(existing.getStatusDescription()).isEqualTo("ACTIVE"); // Preserved (ignored in mapper)
    }

    @Test
    void updateAddressFromDto_withNullValues_shouldNotUpdateExisting() {
        // Given
        Address existing = Address.builder()
                .id(1L)
                .typeCode(3)
                .addressLine1("Old Street")
                .addressLine2("Old Floor")
                .postalCode("OLD 123")
                .city("OldCity")
                .country("OldCountry")
                .build();

        AddressCreateDTO updateDTO = AddressCreateDTO.builder()
                .typeCode(3)
                .addressLine1("Updated Street")
                // addressLine2 is null
                // postalCode is null
                .city("NewCity")
                // country is null
                .build();

        // When
        mapper.updateAddressFromDto(updateDTO, existing);

        // Then
        assertThat(existing.getAddressLine1()).isEqualTo("Updated Street"); // Updated
        assertThat(existing.getAddressLine2()).isEqualTo("Old Floor"); // Not updated (null in DTO)
        assertThat(existing.getPostalCode()).isEqualTo("OLD 123"); // Not updated (null in DTO)
        assertThat(existing.getCity()).isEqualTo("NewCity"); // Updated
        assertThat(existing.getCountry()).isEqualTo("OldCountry"); // Not updated (null in DTO)
    }

    @Test
    void getAddressTypeName_withAllValidCodes_shouldReturnCorrectNames() {
        // Test all valid address type codes
        assertThat(mapper.getAddressTypeName(1)).isEqualTo("RESIDENTIAL");
        assertThat(mapper.getAddressTypeName(2)).isEqualTo("BUSINESS");
        assertThat(mapper.getAddressTypeName(3)).isEqualTo("REGISTERED");
        assertThat(mapper.getAddressTypeName(4)).isEqualTo("CORRESPONDENCE");
        assertThat(mapper.getAddressTypeName(5)).isEqualTo("POSTAL");
    }

    @Test
    void getAddressTypeName_withNullCode_shouldReturnNull() {
        // When
        String result = mapper.getAddressTypeName(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getAddressTypeName_withInvalidCode_shouldReturnUnknown() {
        // When
        String result = mapper.getAddressTypeName(99);

        // Then
        assertThat(result).isEqualTo("UNKNOWN");
    }

    @Test
    void getAddressTypeName_withZeroCode_shouldReturnUnknown() {
        // When
        String result = mapper.getAddressTypeName(0);

        // Then
        assertThat(result).isEqualTo("UNKNOWN");
    }

    @Test
    void getAddressTypeName_withNegativeCode_shouldReturnUnknown() {
        // When
        String result = mapper.getAddressTypeName(-1);

        // Then
        assertThat(result).isEqualTo("UNKNOWN");
    }

    @Test
    void toAddress_withAllTypesCodes_shouldMapCorrectly() {
        // Test all valid type codes
        for (int i = 1; i <= 5; i++) {
            AddressCreateDTO dto = AddressCreateDTO.builder()
                    .typeCode(i)
                    .addressLine1("Test Street")
                    .city("Test City")
                    .country("Test Country")
                    .build();

            Address address = mapper.toAddress(dto);

            assertThat(address.getTypeCode()).isEqualTo(i);
        }
    }

    @Test
    void toAddressDTO_withMinimalFields_shouldMapSuccessfully() {
        // Given
        Address address = Address.builder()
                .id(1L)
                .typeCode(1)
                .addressLine1("Simple Street")
                .city("Simple City")
                .country("Simple Country")
                .build();

        // When
        AddressDTO dto = mapper.toAddressDTO(address);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAddressLine1()).isEqualTo("Simple Street");
        assertThat(dto.getAddressLine2()).isNull();
        assertThat(dto.getPostalCode()).isNull();
        assertThat(dto.getStateCode()).isNull();
    }

    @Test
    void toAddress_withMinimalCreateDTO_shouldMapSuccessfully() {
        // Given
        AddressCreateDTO dto = AddressCreateDTO.builder()
                .typeCode(2)
                .addressLine1("Minimal Street")
                .city("Minimal City")
                .country("Minimal Country")
                .build();

        // When
        Address address = mapper.toAddress(dto);

        // Then
        assertThat(address).isNotNull();
        assertThat(address.getTypeCode()).isEqualTo(2);
        assertThat(address.getAddressLine1()).isEqualTo("Minimal Street");
        assertThat(address.getAddressLine2()).isNull();
        // statusDescription will be set to "ACTIVE" by @PrePersist when persisted
    }
}
