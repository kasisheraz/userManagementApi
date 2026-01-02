package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.Address;
import com.fincore.usermgmt.entity.AddressType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    private Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .typeCode(AddressType.BUSINESS.getCode())
                .addressLine1("123 Business Street")
                .addressLine2("Suite 456")
                .city("London")
                .postalCode("SW1A 1AA")
                .stateCode("Greater London")
                .country("United Kingdom")
                .statusDescription("ACTIVE")
                .createdBy(1L)
                .build();
        address = addressRepository.save(address);
    }

    @Test
    void findByTypeCode_Found() {
        List<Address> result = addressRepository.findByTypeCode(AddressType.BUSINESS.getCode());
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("123 Business Street", result.get(0).getAddressLine1());
    }

    @Test
    void findByTypeCode_NotFound() {
        List<Address> result = addressRepository.findByTypeCode(AddressType.RESIDENTIAL.getCode());
        
        assertTrue(result.isEmpty());
    }

    @Test
    void findByCountry_Found() {
        List<Address> result = addressRepository.findByCountry("United Kingdom");
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByCountry_NotFound() {
        List<Address> result = addressRepository.findByCountry("USA");
        
        assertTrue(result.isEmpty());
    }

    @Test
    void findByCity_Found() {
        List<Address> result = addressRepository.findByCity("London");
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByCity_NotFound() {
        List<Address> result = addressRepository.findByCity("Paris");
        
        assertTrue(result.isEmpty());
    }

    @Test
    void findByPostalCode_Found() {
        List<Address> result = addressRepository.findByPostalCode("SW1A 1AA");
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByStatusDescription_Found() {
        List<Address> result = addressRepository.findByStatusDescription("ACTIVE");
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByCreatedBy_Found() {
        List<Address> result = addressRepository.findByCreatedBy(1L);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void findByCreatedBy_NotFound() {
        List<Address> result = addressRepository.findByCreatedBy(999L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void saveMultipleAddresses_Success() {
        Address residentialAddress = Address.builder()
                .typeCode(AddressType.RESIDENTIAL.getCode())
                .addressLine1("456 Home Lane")
                .city("Manchester")
                .country("United Kingdom")
                .statusDescription("ACTIVE")
                .build();
        addressRepository.save(residentialAddress);

        Address registeredAddress = Address.builder()
                .typeCode(AddressType.REGISTERED.getCode())
                .addressLine1("789 Registered Road")
                .city("Birmingham")
                .country("United Kingdom")
                .statusDescription("ACTIVE")
                .build();
        addressRepository.save(registeredAddress);

        List<Address> ukAddresses = addressRepository.findByCountry("United Kingdom");
        assertEquals(3, ukAddresses.size());

        List<Address> businessAddresses = addressRepository.findByTypeCode(AddressType.BUSINESS.getCode());
        assertEquals(1, businessAddresses.size());

        List<Address> residentialAddresses = addressRepository.findByTypeCode(AddressType.RESIDENTIAL.getCode());
        assertEquals(1, residentialAddresses.size());
    }
}
