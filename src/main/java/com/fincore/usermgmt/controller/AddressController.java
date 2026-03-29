package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Address Management", description = "APIs for managing addresses for users and organisations")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    /**
     * Create a new address.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new address",
        description = "Creates a new address with the provided information including street, city, country, and postal code"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Address created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AddressDTO> createAddress(
            @Parameter(description = "Address creation data", required = true)
            @Valid @RequestBody AddressCreateDTO createDTO) {
        log.info("REST request to create address");
        AddressDTO created = addressService.createAddress(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get address by ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get address by ID",
        description = "Retrieves a specific address by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved address",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
        @ApiResponse(responseCode = "404", description = "Address not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AddressDTO> getAddressById(
        @Parameter(description = "Address ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("REST request to get address by ID: {}", id);
        return addressService.getAddressById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all addresses.
     */
    @GetMapping
    @Operation(
        summary = "Get all addresses",
        description = "Retrieves a list of all addresses in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of addresses",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        log.info("REST request to get all addresses");
        List<AddressDTO> addresses = addressService.getAllAddresses();
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get addresses by type.
     */
    @GetMapping("/type/{type}")
    @Operation(
        summary = "Get addresses by type",
        description = "Retrieves addresses filtered by type (e.g., RESIDENTIAL, BUSINESS, MAILING)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved addresses",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid address type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<AddressDTO>> getAddressesByType(
        @Parameter(description = "Address type", required = true, example = "RESIDENTIAL")
        @PathVariable String type
    ) {
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
    @Operation(
        summary = "Get addresses by country",
        description = "Retrieves addresses filtered by country name"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved addresses",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<AddressDTO>> getAddressesByCountry(
        @Parameter(description = "Country name", required = true, example = "United States")
        @PathVariable String country
    ) {
        log.info("REST request to get addresses by country: {}", country);
        List<AddressDTO> addresses = addressService.getAddressesByCountry(country);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get addresses by city.
     */
    @GetMapping("/city/{city}")
    @Operation(
        summary = "Get addresses by city",
        description = "Retrieves addresses filtered by city name"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved addresses",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<AddressDTO>> getAddressesByCity(
        @Parameter(description = "City name", required = true, example = "New York")
        @PathVariable String city
    ) {
        log.info("REST request to get addresses by city: {}", city);
        List<AddressDTO> addresses = addressService.getAddressesByCity(city);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Update an address.
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an address",
        description = "Updates an existing address with new information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Address updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AddressDTO.class))),
        @ApiResponse(responseCode = "404", description = "Address not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AddressDTO> updateAddress(
            @Parameter(description = "Address ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated address data", required = true)
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
    @Operation(
        summary = "Delete an address",
        description = "Deletes an address by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Address deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Address not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAddress(
        @Parameter(description = "Address ID", required = true, example = "1")
        @PathVariable Long id
    ) {
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
