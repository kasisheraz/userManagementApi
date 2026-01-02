package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.entity.Address;
import com.fincore.usermgmt.entity.AddressType;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Address management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    /**
     * Create a new address.
     */
    @Transactional
    public AddressDTO createAddress(AddressCreateDTO createDTO) {
        log.info("Creating new address");

        Address address = addressMapper.toAddress(createDTO);
        Address saved = addressRepository.save(address);
        
        log.info("Created address with ID: {}", saved.getId());
        return addressMapper.toAddressDTO(saved);
    }

    /**
     * Get address by ID.
     */
    @Transactional(readOnly = true)
    public Optional<AddressDTO> getAddressById(Long id) {
        log.debug("Fetching address by ID: {}", id);
        return addressRepository.findById(id)
                .map(addressMapper::toAddressDTO);
    }

    /**
     * Get all addresses.
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getAllAddresses() {
        log.debug("Fetching all addresses");
        return addressRepository.findAll().stream()
                .map(addressMapper::toAddressDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get addresses by type.
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByType(String typeStr) {
        log.debug("Fetching addresses by type: {}", typeStr);
        AddressType type = AddressType.valueOf(typeStr.toUpperCase());
        return addressRepository.findByTypeCode(type.getCode()).stream()
                .map(addressMapper::toAddressDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get addresses by country.
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByCountry(String country) {
        log.debug("Fetching addresses by country: {}", country);
        return addressRepository.findByCountry(country).stream()
                .map(addressMapper::toAddressDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get addresses by city.
     */
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByCity(String city) {
        log.debug("Fetching addresses by city: {}", city);
        return addressRepository.findByCity(city).stream()
                .map(addressMapper::toAddressDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update an address.
     */
    @Transactional
    public AddressDTO updateAddress(Long id, AddressCreateDTO updateDTO) {
        log.info("Updating address ID: {}", id);

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + id));

        addressMapper.updateAddressFromDto(updateDTO, address);

        Address saved = addressRepository.save(address);
        log.info("Updated address ID: {}", saved.getId());
        
        return addressMapper.toAddressDTO(saved);
    }

    /**
     * Delete an address.
     */
    @Transactional
    public void deleteAddress(Long id) {
        log.info("Deleting address ID: {}", id);

        if (!addressRepository.existsById(id)) {
            throw new RuntimeException("Address not found with ID: " + id);
        }

        addressRepository.deleteById(id);
        log.info("Deleted address ID: {}", id);
    }
}
