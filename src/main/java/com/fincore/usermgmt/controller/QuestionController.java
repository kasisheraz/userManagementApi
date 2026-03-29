package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.dto.QuestionnaireQuestionResponseDTO;
import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.mapper.KycAmlMapper;
import com.fincore.usermgmt.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Questions (alias for Questionnaires)
 * This endpoint provides the same functionality as /api/questionnaires
 * for backwards compatibility with UI expectations
 */
@Slf4j
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Question Management (Alias)", description = "Alias endpoint for questionnaire questions - provides the same functionality as /api/questionnaires for backwards compatibility")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {

    private final QuestionnaireService questionnaireService;
    private final KycAmlMapper mapper;

    /**
     * Get all questions
     * GET /api/questions
     */
    @GetMapping
    @Operation(
        summary = "Get all questions",
        description = "Retrieves all questionnaire questions in the system (alias for /api/questionnaires)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of questions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<QuestionnaireQuestionResponseDTO>> getAllQuestions() {
        log.info("Fetching all questions via /api/questions endpoint");

        List<QuestionnaireQuestion> questions = questionnaireService.getAllQuestions();
        List<QuestionnaireQuestionResponseDTO> response = questions.stream()
                .map(mapper::toQuestionnaireQuestionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get question by ID
     * GET /api/questions/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get question by ID",
        description = "Retrieves a specific questionnaire question by its unique identifier (alias for /api/questionnaires/{id})"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved question",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Question not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuestionnaireQuestionResponseDTO> getQuestion(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("Fetching question {} via /api/questions endpoint", id);

        QuestionnaireQuestion question = questionnaireService.getQuestionById(id);
        return ResponseEntity.ok(mapper.toQuestionnaireQuestionResponseDTO(question));
    }

    /**
     * Get active questions
     * GET /api/questions/active
     */
    @GetMapping("/active")
    @Operation(
        summary = "Get active questions",
        description = "Retrieves all active questionnaire questions (alias for /api/questionnaires/active)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active questions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<QuestionnaireQuestionResponseDTO>> getActiveQuestions() {
        log.info("Fetching active questions via /api/questions endpoint");

        List<QuestionnaireQuestion> questions = questionnaireService.getActiveQuestions();
        List<QuestionnaireQuestionResponseDTO> response = questions.stream()
                .map(mapper::toQuestionnaireQuestionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
