package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.entity.Address;
import com.fincore.usermgmt.entity.AddressType;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private Address address;
    private AddressDTO addressDTO;
    private AddressCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .id(1L)
                .typeCode(AddressType.BUSINESS.getCode())
                .addressLine1("123 Business Street")
                .addressLine2("Suite 456")
                .city("London")
                .postalCode("SW1A 1AA")
                .country("United Kingdom")
                .statusDescription("ACTIVE")
                .build();

        addressDTO = AddressDTO.builder()
                .id(1L)
                .typeCode(AddressType.BUSINESS.getCode())
                .addressType("BUSINESS")
                .addressLine1("123 Business Street")
                .addressLine2("Suite 456")
                .city("London")
                .postalCode("SW1A 1AA")
                .country("United Kingdom")
                .statusDescription("ACTIVE")
                .build();

        createDTO = AddressCreateDTO.builder()
                .typeCode(AddressType.BUSINESS.getCode())
                .addressLine1("123 Business Street")
                .addressLine2("Suite 456")
                .city("London")
                .postalCode("SW1A 1AA")
                .country("United Kingdom")
                .build();
    }

    @Test
    void createAddress_Success() {
        when(addressMapper.toAddress(any(AddressCreateDTO.class))).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toAddressDTO(any(Address.class))).thenReturn(addressDTO);

        AddressDTO result = addressService.createAddress(createDTO);

        assertNotNull(result);
        assertEquals("123 Business Street", result.getAddressLine1());
        assertEquals("United Kingdom", result.getCountry());
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void getAddressById_Found() {
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        Optional<AddressDTO> result = addressService.getAddressById(1L);

        assertTrue(result.isPresent());
        assertEquals("London", result.get().getCity());
    }

    @Test
    void getAddressById_NotFound() {
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<AddressDTO> result = addressService.getAddressById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllAddresses_Success() {
        when(addressRepository.findAll()).thenReturn(Arrays.asList(address));
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        List<AddressDTO> result = addressService.getAllAddresses();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAddressesByType_Success() {
        when(addressRepository.findByTypeCode(AddressType.BUSINESS.getCode()))
                .thenReturn(Arrays.asList(address));
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        List<AddressDTO> result = addressService.getAddressesByType("BUSINESS");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BUSINESS", result.get(0).getAddressType());
    }

    @Test
    void getAddressesByCountry_Success() {
        when(addressRepository.findByCountry("United Kingdom")).thenReturn(Arrays.asList(address));
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        List<AddressDTO> result = addressService.getAddressesByCountry("United Kingdom");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAddressesByCity_Success() {
        when(addressRepository.findByCity("London")).thenReturn(Arrays.asList(address));
        when(addressMapper.toAddressDTO(address)).thenReturn(addressDTO);

        List<AddressDTO> result = addressService.getAddressesByCity("London");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateAddress_Success() {
        AddressCreateDTO updateDTO = AddressCreateDTO.builder()
                .typeCode(AddressType.BUSINESS.getCode())
                .addressLine1("456 Updated Street")
                .city("Manchester")
                .country("United Kingdom")
                .build();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toAddressDTO(any(Address.class))).thenReturn(addressDTO);

        AddressDTO result = addressService.updateAddress(1L, updateDTO);

        assertNotNull(result);
        verify(addressMapper).updateAddressFromDto(updateDTO, address);
    }

    @Test
    void updateAddress_NotFound() {
        AddressCreateDTO updateDTO = new AddressCreateDTO();
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            addressService.updateAddress(1L, updateDTO));
        
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void deleteAddress_Success() {
        when(addressRepository.existsById(1L)).thenReturn(true);

        addressService.deleteAddress(1L);

        verify(addressRepository).deleteById(1L);
    }

    @Test
    void deleteAddress_NotFound() {
        when(addressRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            addressService.deleteAddress(1L));
        
        assertTrue(exception.getMessage().contains("not found"));
    }
}
