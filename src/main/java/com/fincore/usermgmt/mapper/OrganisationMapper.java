package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.OrganisationCreateDTO;
import com.fincore.usermgmt.dto.OrganisationDTO;
import com.fincore.usermgmt.dto.OrganisationUpdateDTO;
import com.fincore.usermgmt.entity.Organisation;
import com.fincore.usermgmt.entity.OrganisationStatus;
import com.fincore.usermgmt.entity.OrganisationType;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Organisation entity and DTOs.
 */
@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface OrganisationMapper {

    OrganisationMapper INSTANCE = Mappers.getMapper(OrganisationMapper.class);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", expression = "java(getOwnerFullName(organisation))")
    @Mapping(target = "organisationType", source = "organisationType", qualifiedByName = "organisationTypeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "registeredAddress", source = "registeredAddress")
    @Mapping(target = "businessAddress", source = "businessAddress")
    @Mapping(target = "correspondenceAddress", source = "correspondenceAddress")
    OrganisationDTO toOrganisationDTO(Organisation organisation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "registeredAddress", ignore = true)
    @Mapping(target = "businessAddress", ignore = true)
    @Mapping(target = "correspondenceAddress", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDatetime", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "organisationType", source = "organisationType", qualifiedByName = "stringToOrganisationType")
    Organisation toOrganisation(OrganisationCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "registeredAddress", ignore = true)
    @Mapping(target = "businessAddress", ignore = true)
    @Mapping(target = "correspondenceAddress", ignore = true)
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDatetime", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "organisationType", source = "organisationType", qualifiedByName = "stringToOrganisationType")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrganisationFromDto(OrganisationUpdateDTO dto, @MappingTarget Organisation organisation);

    /**
     * Get owner full name from organisation.
     */
    default String getOwnerFullName(Organisation organisation) {
        if (organisation.getOwner() == null) return null;
        String firstName = organisation.getOwner().getFirstName() != null ? organisation.getOwner().getFirstName() : "";
        String lastName = organisation.getOwner().getLastName() != null ? organisation.getOwner().getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    @Named("organisationTypeToString")
    default String organisationTypeToString(OrganisationType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToOrganisationType")
    default OrganisationType stringToOrganisationType(String type) {
        if (type == null || type.isEmpty()) return null;
        try {
            return OrganisationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OrganisationType.OTHER;
        }
    }

    @Named("statusToString")
    default String statusToString(OrganisationStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default OrganisationStatus stringToStatus(String status) {
        if (status == null || status.isEmpty()) return null;
        try {
            return OrganisationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
