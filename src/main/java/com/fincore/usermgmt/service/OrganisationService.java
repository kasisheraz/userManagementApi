package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.entity.*;
import com.fincore.usermgmt.mapper.AddressMapper;
import com.fincore.usermgmt.mapper.OrganisationMapper;
import com.fincore.usermgmt.repository.AddressRepository;
import com.fincore.usermgmt.repository.OrganisationRepository;
import com.fincore.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Organisation management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrganisationMapper organisationMapper;
    private final AddressMapper addressMapper;

    /**
     * Create a new organisation.
     */
    @Transactional
    public OrganisationDTO createOrganisation(OrganisationCreateDTO createDTO) {
        log.info("Creating new organisation: {}", createDTO.getLegalName());

        // Validate owner exists
        User owner = userRepository.findById(createDTO.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner user not found with ID: " + createDTO.getOwnerId()));

        // Check for duplicate registration number
        if (createDTO.getRegistrationNumber() != null && 
            organisationRepository.existsByRegistrationNumber(createDTO.getRegistrationNumber())) {
            throw new RuntimeException("Organisation with registration number already exists: " + createDTO.getRegistrationNumber());
        }

        // Check for duplicate company number
        if (createDTO.getCompanyNumber() != null && 
            organisationRepository.existsByCompanyNumber(createDTO.getCompanyNumber())) {
            throw new RuntimeException("Organisation with company number already exists: " + createDTO.getCompanyNumber());
        }

        Organisation organisation = organisationMapper.toOrganisation(createDTO);
        organisation.setOwner(owner);

        // Handle addresses
        if (createDTO.getRegisteredAddress() != null) {
            Address registeredAddress = addressMapper.toAddress(createDTO.getRegisteredAddress());
            registeredAddress.setAddressType(AddressType.REGISTERED);
            organisation.setRegisteredAddress(registeredAddress);
        }

        if (createDTO.getBusinessAddress() != null) {
            Address businessAddress = addressMapper.toAddress(createDTO.getBusinessAddress());
            businessAddress.setAddressType(AddressType.BUSINESS);
            organisation.setBusinessAddress(businessAddress);
        }

        if (createDTO.getCorrespondenceAddress() != null) {
            Address correspondenceAddress = addressMapper.toAddress(createDTO.getCorrespondenceAddress());
            correspondenceAddress.setAddressType(AddressType.CORRESPONDENCE);
            organisation.setCorrespondenceAddress(correspondenceAddress);
        }

        Organisation saved = organisationRepository.save(organisation);
        log.info("Created organisation with ID: {}", saved.getId());
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Get organisation by ID.
     */
    @Transactional(readOnly = true)
    public Optional<OrganisationDTO> getOrganisationById(Long id) {
        log.debug("Fetching organisation by ID: {}", id);
        return organisationRepository.findById(id)
                .map(organisationMapper::toOrganisationDTO);
    }

    /**
     * Get all organisations with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrganisationDTO> getAllOrganisations(int page, int size, String sortBy, String sortDirection) {
        log.debug("Fetching all organisations - page: {}, size: {}", page, size);
        
        Sort sort = sortDirection.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Organisation> organisationPage = organisationRepository.findAll(pageable);
        
        return buildPagedResponse(organisationPage);
    }

    /**
     * Search organisations with filters.
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrganisationDTO> searchOrganisations(OrganisationSearchDTO searchDTO) {
        log.debug("Searching organisations with criteria: {}", searchDTO);
        
        Sort sort = searchDTO.getSortDirection().equalsIgnoreCase("DESC") 
                ? Sort.by(searchDTO.getSortBy()).descending() 
                : Sort.by(searchDTO.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        OrganisationStatus status = null;
        if (searchDTO.getStatus() != null && !searchDTO.getStatus().isEmpty()) {
            try {
                status = OrganisationStatus.valueOf(searchDTO.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", searchDTO.getStatus());
            }
        }

        OrganisationType type = null;
        if (searchDTO.getOrganisationType() != null && !searchDTO.getOrganisationType().isEmpty()) {
            try {
                type = OrganisationType.valueOf(searchDTO.getOrganisationType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid organisation type filter: {}", searchDTO.getOrganisationType());
            }
        }

        Page<Organisation> organisationPage = organisationRepository.searchOrganisations(
                searchDTO.getSearchTerm(), status, type, pageable);
        
        return buildPagedResponse(organisationPage);
    }

    /**
     * Get organisations by owner user ID.
     */
    @Transactional(readOnly = true)
    public List<OrganisationDTO> getOrganisationsByOwner(Long ownerId) {
        log.debug("Fetching organisations for owner: {}", ownerId);
        return organisationRepository.findByOwnerId(ownerId).stream()
                .map(organisationMapper::toOrganisationDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get organisations by status.
     */
    @Transactional(readOnly = true)
    public List<OrganisationDTO> getOrganisationsByStatus(String statusStr) {
        log.debug("Fetching organisations by status: {}", statusStr);
        OrganisationStatus status = OrganisationStatus.valueOf(statusStr.toUpperCase());
        return organisationRepository.findByStatus(status).stream()
                .map(organisationMapper::toOrganisationDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update an organisation.
     */
    @Transactional
    public OrganisationDTO updateOrganisation(Long id, OrganisationUpdateDTO updateDTO) {
        log.info("Updating organisation ID: {}", id);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + id));

        // Check for duplicate registration number (if changed)
        if (updateDTO.getRegistrationNumber() != null && 
            !updateDTO.getRegistrationNumber().equals(organisation.getRegistrationNumber()) &&
            organisationRepository.existsByRegistrationNumber(updateDTO.getRegistrationNumber())) {
            throw new RuntimeException("Organisation with registration number already exists: " + updateDTO.getRegistrationNumber());
        }

        organisationMapper.updateOrganisationFromDto(updateDTO, organisation);

        // Handle address updates
        if (updateDTO.getRegisteredAddress() != null) {
            if (organisation.getRegisteredAddress() != null) {
                addressMapper.updateAddressFromDto(updateDTO.getRegisteredAddress(), organisation.getRegisteredAddress());
            } else {
                Address registeredAddress = addressMapper.toAddress(updateDTO.getRegisteredAddress());
                registeredAddress.setAddressType(AddressType.REGISTERED);
                organisation.setRegisteredAddress(registeredAddress);
            }
        }

        if (updateDTO.getBusinessAddress() != null) {
            if (organisation.getBusinessAddress() != null) {
                addressMapper.updateAddressFromDto(updateDTO.getBusinessAddress(), organisation.getBusinessAddress());
            } else {
                Address businessAddress = addressMapper.toAddress(updateDTO.getBusinessAddress());
                businessAddress.setAddressType(AddressType.BUSINESS);
                organisation.setBusinessAddress(businessAddress);
            }
        }

        if (updateDTO.getCorrespondenceAddress() != null) {
            if (organisation.getCorrespondenceAddress() != null) {
                addressMapper.updateAddressFromDto(updateDTO.getCorrespondenceAddress(), organisation.getCorrespondenceAddress());
            } else {
                Address correspondenceAddress = addressMapper.toAddress(updateDTO.getCorrespondenceAddress());
                correspondenceAddress.setAddressType(AddressType.CORRESPONDENCE);
                organisation.setCorrespondenceAddress(correspondenceAddress);
            }
        }

        Organisation saved = organisationRepository.save(organisation);
        log.info("Updated organisation ID: {}", saved.getId());
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Update organisation status.
     */
    @Transactional
    public OrganisationDTO updateOrganisationStatus(Long id, String statusStr, String reason) {
        log.info("Updating organisation status - ID: {}, Status: {}", id, statusStr);
        
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with ID: " + id));

        OrganisationStatus newStatus = OrganisationStatus.valueOf(statusStr.toUpperCase());
        organisation.setStatus(newStatus);
        organisation.setReasonDescription(reason);

        Organisation saved = organisationRepository.save(organisation);
        log.info("Updated organisation status to: {}", newStatus);
        
        return organisationMapper.toOrganisationDTO(saved);
    }

    /**
     * Delete an organisation.
     */
    @Transactional
    public void deleteOrganisation(Long id) {
        log.info("Deleting organisation ID: {}", id);
        
        if (!organisationRepository.existsById(id)) {
            throw new RuntimeException("Organisation not found with ID: " + id);
        }
        
        organisationRepository.deleteById(id);
        log.info("Deleted organisation ID: {}", id);
    }

    /**
     * Check if organisation exists by registration number.
     */
    @Transactional(readOnly = true)
    public boolean existsByRegistrationNumber(String registrationNumber) {
        return organisationRepository.existsByRegistrationNumber(registrationNumber);
    }

    /**
     * Build paginated response from Page object.
     */
    private PagedResponse<OrganisationDTO> buildPagedResponse(Page<Organisation> page) {
        List<OrganisationDTO> content = page.getContent().stream()
                .map(organisationMapper::toOrganisationDTO)
                .collect(Collectors.toList());

        return PagedResponse.<OrganisationDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
