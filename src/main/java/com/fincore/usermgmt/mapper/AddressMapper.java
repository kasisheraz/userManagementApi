package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.entity.Address;
import com.fincore.usermgmt.entity.AddressType;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for Address entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    @Mapping(target = "addressType", expression = "java(getAddressTypeName(address.getTypeCode()))")
    AddressDTO toAddressDTO(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusDescription", constant = "ACTIVE")
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Address toAddress(AddressCreateDTO addressCreateDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statusDescription", ignore = true)
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromDto(AddressCreateDTO dto, @MappingTarget Address address);

    /**
     * Helper method to get address type name from code.
     */
    default String getAddressTypeName(Integer typeCode) {
        if (typeCode == null) return null;
        try {
            return AddressType.fromCode(typeCode).name();
        } catch (IllegalArgumentException e) {
            return "UNKNOWN";
        }
    }
}
