package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.dto.QuestionnaireQuestionRequestDTO;
import com.fincore.usermgmt.dto.QuestionnaireQuestionResponseDTO;
import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.QuestionCategory;
import com.fincore.usermgmt.service.QuestionnaireService;
import com.fincore.usermgmt.mapper.KycAmlMapper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Questionnaire endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/questionnaires")
@RequiredArgsConstructor
@Tag(name = "Questionnaire Management", description = "APIs for managing questionnaire questions used in KYC/AML processes including creation, activation, and categorization")
@SecurityRequirement(name = "bearerAuth")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final KycAmlMapper mapper;

    /**
     * Get all questions
     * GET /api/v1/questions
     */
    @GetMapping
    @Operation(
        summary = "Get all questionnaire questions",
        description = "Retrieves all questionnaire questions in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of questions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<QuestionnaireQuestionResponseDTO>> getAllQuestions() {
        log.info("Fetching all questions");

        List<QuestionnaireQuestion> questions = questionnaireService.getAllQuestions();
        List<QuestionnaireQuestionResponseDTO> response = questions.stream()
                .map(mapper::toQuestionnaireQuestionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get question by ID
     * GET /api/v1/questions/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get questionnaire question by ID",
        description = "Retrieves a specific questionnaire question by its unique identifier"
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
        log.info("Fetching question: {}", id);

        QuestionnaireQuestion question = questionnaireService.getQuestionById(id);
        return ResponseEntity.ok(mapper.toQuestionnaireQuestionResponseDTO(question));
    }

    /**
     * Get active questions
     * GET /api/v1/questions/active
     */
    @GetMapping("/active")
    @Operation(
        summary = "Get active questionnaire questions",
        description = "Retrieves all active questionnaire questions that are currently in use"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active questions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<QuestionnaireQuestionResponseDTO>> getActiveQuestions() {
        log.info("Fetching active questions");

        List<QuestionnaireQuestion> questions = questionnaireService.getActiveQuestions();
        List<QuestionnaireQuestionResponseDTO> response = questions.stream()
                .map(mapper::toQuestionnaireQuestionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get questions by category
     * GET /api/v1/questions/category/{category}
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get questions by category",
        description = "Retrieves active questionnaire questions filtered by category (PERSONAL, BUSINESS, FINANCIAL, COMPLIANCE, OTHER)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved questions",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid category value",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<QuestionnaireQuestionResponseDTO>> getQuestionsByCategory(
            @Parameter(description = "Question category", required = true, example = "PERSONAL")
            @PathVariable String category) {

        log.info("Fetching questions with category: {}", category);

        List<QuestionnaireQuestion> questions = questionnaireService
                .getActiveQuestionsByCategory(QuestionCategory.valueOf(category));

        List<QuestionnaireQuestionResponseDTO> response = questions.stream()
                .map(mapper::toQuestionnaireQuestionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create a new question
     * POST /api/questionnaires
     * Supports both new format {name, description} and legacy format {questionText, questionCategory, displayOrder}
     */
    @PostMapping
    @Operation(
        summary = "Create a new questionnaire question",
        description = "Creates a new questionnaire question. Supports both new format (name, description) and legacy format (questionText, questionCategory, displayOrder)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Question created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data - name or questionText is required",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuestionnaireQuestionResponseDTO> createQuestion(
            @Parameter(description = "Question creation data (supports multiple formats)", required = true)
            @RequestBody Map<String, Object> request,
            @Parameter(description = "User ID of the creator (optional)", example = "123")
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        String questionText;
        String questionCategory;
        Integer displayOrder;

        // Support frontend format: {name, description, version, isActive}
        if (request.containsKey("name")) {
            questionText = (String) request.get("name");
            if (questionText == null || questionText.trim().isEmpty()) {
                throw new IllegalArgumentException("name is required");
            }
            // Use description as additional context if provided
            String description = (String) request.get("description");
            if (description != null && !description.trim().isEmpty()) {
                questionText = questionText + ": " + description;
            }
            questionCategory = request.getOrDefault("category", "PERSONAL").toString();
            displayOrder = request.containsKey("displayOrder") ? 
                Integer.parseInt(request.get("displayOrder").toString()) : 1;
        } 
        // Support legacy format: {questionText, questionCategory, displayOrder}
        else {
            questionText = (String) request.get("questionText");
            if (questionText == null || questionText.trim().isEmpty()) {
                throw new IllegalArgumentException("questionText is required");
            }
            questionCategory = (String) request.get("questionCategory");
            displayOrder = request.containsKey("displayOrder") ? 
                Integer.parseInt(request.get("displayOrder").toString()) : 1;
        }

        log.info("Creating new question: {}", questionText);

        User creator = userId != null ? User.builder().id(userId).build() : null;

        QuestionnaireQuestion question = questionnaireService.createQuestion(
                questionText,
                QuestionCategory.valueOf(questionCategory != null ? questionCategory : "OTHER"),
                displayOrder,
                creator
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toQuestionnaireQuestionResponseDTO(question));
    }

    /**
     * Update question
     * PUT /api/v1/questions/{id}
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update a questionnaire question",
        description = "Updates an existing questionnaire question's text, category, or display order"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Question updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Question not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuestionnaireQuestionResponseDTO> updateQuestion(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Question update data", required = true)
            @RequestBody QuestionnaireQuestionRequestDTO request,
            @Parameter(description = "User ID of the modifier (optional)", example = "123")
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("Updating question: {}", id);

        User modifiedBy = userId != null ? User.builder().id(userId).build() : null;

        QuestionnaireQuestion updated = questionnaireService.updateQuestion(
                id,
                request.getQuestionText(),
                request.getQuestionCategory() != null ? QuestionCategory.valueOf(request.getQuestionCategory()) : null,
                null,
                request.getDisplayOrder(),
                modifiedBy
        );

        return ResponseEntity.ok(mapper.toQuestionnaireQuestionResponseDTO(updated));
    }

    /**
     * Activate question
     * PATCH /api/v1/questions/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    @Operation(
        summary = "Activate a questionnaire question",
        description = "Activates a questionnaire question, making it available for use in questionnaires"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Question activated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Question not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuestionnaireQuestionResponseDTO> activateQuestion(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "User ID of the modifier (optional)", example = "123")
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("Activating question: {}", id);

        User modifiedBy = userId != null ? User.builder().id(userId).build() : null;
        QuestionnaireQuestion updated = questionnaireService.activateQuestion(id, modifiedBy);

        return ResponseEntity.ok(mapper.toQuestionnaireQuestionResponseDTO(updated));
    }

    /**
     * Inactivate question
     * PATCH /api/v1/questions/{id}/inactivate
     */
    @PatchMapping("/{id}/inactivate")
    @Operation(
        summary = "Inactivate a questionnaire question",
        description = "Inactivates a questionnaire question, removing it from active use while preserving historical data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Question inactivated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuestionnaireQuestionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Question not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuestionnaireQuestionResponseDTO> inactivateQuestion(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "User ID of the modifier (optional)", example = "123")
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("Inactivating question: {}", id);

        User modifiedBy = userId != null ? User.builder().id(userId).build() : null;
        QuestionnaireQuestion updated = questionnaireService.inactivateQuestion(id, modifiedBy);

        return ResponseEntity.ok(mapper.toQuestionnaireQuestionResponseDTO(updated));
    }

    /**
     * Delete question
     * DELETE /api/v1/questions/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a questionnaire question",
        description = "Permanently deletes a questionnaire question from the system. Use with caution as this may affect historical data"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Question deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Question not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Integer id) {
        log.info("Deleting question: {}", id);

        questionnaireService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get active questions count
     * GET /api/v1/questions/count/active
     */
    @GetMapping("/count/active")
    @Operation(
        summary = "Count active questionnaire questions",
        description = "Returns the total number of active questionnaire questions in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved count",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Long> getActiveQuestionsCount() {
        log.info("Fetching active questions count");

        long count = questionnaireService.getActiveQuestionsCount();
        return ResponseEntity.ok(count);
    }
}
