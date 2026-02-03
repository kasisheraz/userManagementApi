package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireQuestionRequestDTO {
    private String questionText;
    private String questionCategory;
    private Integer displayOrder;
}
