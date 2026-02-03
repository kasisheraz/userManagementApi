package com.fincore.usermgmt.mapper;

import com.fincore.usermgmt.dto.KycVerificationResponseDTO;
import com.fincore.usermgmt.dto.AmlScreeningResponseDTO;
import com.fincore.usermgmt.dto.QuestionnaireQuestionResponseDTO;
import com.fincore.usermgmt.dto.CustomerAnswerResponseDTO;
import com.fincore.usermgmt.entity.CustomerKycVerification;
import com.fincore.usermgmt.entity.AmlScreeningResult;
import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.CustomerAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for KYC/AML entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface KycAmlMapper {

    KycAmlMapper INSTANCE = Mappers.getMapper(KycAmlMapper.class);

    /**
     * Map CustomerKycVerification entity to response DTO
     */
    @Mapping(target = "reviewedById", source = "lastModifiedBy.id")
    @Mapping(target = "userId", source = "user.id")
    KycVerificationResponseDTO toKycVerificationResponseDTO(CustomerKycVerification entity);

    /**
     * Map AmlScreeningResult entity to response DTO
     */
    @Mapping(target = "verificationId", source = "verification.verificationId")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "screeningType", source = "screeningType")
    AmlScreeningResponseDTO toAmlScreeningResponseDTO(AmlScreeningResult entity);

    /**
     * Map QuestionnaireQuestion entity to response DTO
     */
    QuestionnaireQuestionResponseDTO toQuestionnaireQuestionResponseDTO(QuestionnaireQuestion entity);

    /**
     * Map CustomerAnswer entity to response DTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "questionId", source = "question.questionId")
    CustomerAnswerResponseDTO toCustomerAnswerResponseDTO(CustomerAnswer entity);
}
