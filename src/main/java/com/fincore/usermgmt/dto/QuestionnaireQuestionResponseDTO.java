package com.fincore.usermgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireQuestionResponseDTO {
    private Integer questionId;
    private String questionText;
    private String questionCategory;
    private String status;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}
