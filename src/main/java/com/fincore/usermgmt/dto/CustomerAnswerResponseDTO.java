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
public class CustomerAnswerResponseDTO {
    private Long answerId;
    private Long userId;
    private Integer questionId;
    private String answerText;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}
