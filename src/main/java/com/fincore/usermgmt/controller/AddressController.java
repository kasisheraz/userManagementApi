package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Address management endpoints.
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    /**
     * Create a new address.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AddressDTO> createAddress(
            @Valid @RequestBody AddressCreateDTO createDTO) {
        log.info("REST request to create address");
        AddressDTO created = addressService.createAddress(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get address by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long id) {
        log.info("REST request to get address by ID: {}", id);
        return addressService.getAddressById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all addresses.
     */
    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        log.info("REST request to get all addresses");
        List<AddressDTO> addresses = addressService.getAllAddresses();
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get addresses by type.
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<AddressDTO>> getAddressesByType(@PathVariable String type) {
        log.info("REST request to get addresses by type: {}", type);
        try {
            List<AddressDTO> addresses = addressService.getAddressesByType(type);
            return ResponseEntity.ok(addresses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get addresses by country.
     */
    @GetMapping("/country/{country}")
    public ResponseEntity<List<AddressDTO>> getAddressesByCountry(@PathVariable String country) {
        log.info("REST request to get addresses by country: {}", country);
        List<AddressDTO> addresses = addressService.getAddressesByCountry(country);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get addresses by city.
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<AddressDTO>> getAddressesByCity(@PathVariable String city) {
        log.info("REST request to get addresses by city: {}", city);
        List<AddressDTO> addresses = addressService.getAddressesByCity(city);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Update an address.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressCreateDTO updateDTO) {
        log.info("REST request to update address ID: {}", id);
        try {
            AddressDTO updated = addressService.updateAddress(id, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Delete an address.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        log.info("REST request to delete address ID: {}", id);
        try {
            addressService.deleteAddress(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }
}
