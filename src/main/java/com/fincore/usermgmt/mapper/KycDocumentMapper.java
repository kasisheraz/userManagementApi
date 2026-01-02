package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.KycDocumentCreateDTO;
import com.fincore.usermgmt.dto.KycDocumentDTO;
import com.fincore.usermgmt.dto.KycDocumentUpdateDTO;
import com.fincore.usermgmt.entity.DocumentStatus;
import com.fincore.usermgmt.entity.DocumentType;
import com.fincore.usermgmt.entity.KycDocument;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for KycDocument entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface KycDocumentMapper {

    KycDocumentMapper INSTANCE = Mappers.getMapper(KycDocumentMapper.class);

    @Mapping(target = "organisationId", source = "organisation.id")
    @Mapping(target = "organisationName", source = "organisation.legalName")
    @Mapping(target = "documentType", source = "documentType", qualifiedByName = "documentTypeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "verifiedById", source = "verifiedBy.id")
    @Mapping(target = "verifiedByName", expression = "java(getVerifierFullName(document))")
    KycDocumentDTO toKycDocumentDTO(KycDocument document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organisation", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "reasonDescription", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDatetime", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "documentType", source = "documentType", qualifiedByName = "stringToDocumentType")
    KycDocument toKycDocument(KycDocumentCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organisation", ignore = true)
    @Mapping(target = "documentType", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedDatetime", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "verificationIdentifier", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateKycDocumentFromDto(KycDocumentUpdateDTO dto, @MappingTarget KycDocument document);

    /**
     * Get verifier full name from document.
     */
    default String getVerifierFullName(KycDocument document) {
        if (document.getVerifiedBy() == null) return null;
        String firstName = document.getVerifiedBy().getFirstName() != null ? document.getVerifiedBy().getFirstName() : "";
        String lastName = document.getVerifiedBy().getLastName() != null ? document.getVerifiedBy().getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    @Named("documentTypeToString")
    default String documentTypeToString(DocumentType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToDocumentType")
    default DocumentType stringToDocumentType(String type) {
        if (type == null || type.isEmpty()) return null;
        try {
            return DocumentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DocumentType.OTHER;
        }
    }

    @Named("statusToString")
    default String statusToString(DocumentStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default DocumentStatus stringToStatus(String status) {
        if (status == null || status.isEmpty()) return null;
        try {
            return DocumentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
