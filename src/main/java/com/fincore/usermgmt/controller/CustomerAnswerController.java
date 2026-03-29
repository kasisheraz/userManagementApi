package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.CustomerAnswerRequestDTO;
import com.fincore.usermgmt.dto.CustomerAnswerResponseDTO;
import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.entity.CustomerAnswer;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.service.CustomerAnswerService;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Customer Answer endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/customer-answers")
@RequiredArgsConstructor
@Tag(name = "Customer Answers", description = "APIs for managing customer answers to questionnaires and KYC/AML questions")
@SecurityRequirement(name = "bearerAuth")
public class CustomerAnswerController {

    private final CustomerAnswerService answerService;
    private final KycAmlMapper mapper;

    /**
     * Get all customer answers (for admin/overview purposes).
     * GET /api/customer-answers
     */
    @GetMapping
    @Operation(
        summary = "Get all customer answers",
        description = "Retrieves all customer answers in the system (typically for admin or overview purposes)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of customer answers",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<CustomerAnswerResponseDTO>> getAllAnswers() {
        log.info("Fetching all customer answers");
        List<CustomerAnswer> answers = answerService.getAllAnswers();
        List<CustomerAnswerResponseDTO> response = answers.stream()
                .map(mapper::toCustomerAnswerResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Save or update answer
     * POST /api/customer-answers
     */
    @PostMapping
    @Operation(
        summary = "Save or update an answer",
        description = "Creates a new customer answer or updates an existing one for a specific question"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Answer saved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerAnswerResponseDTO> saveAnswer(
            @Parameter(description = "Customer answer data including user ID, question ID, and answer text", required = true)
            @RequestBody CustomerAnswerRequestDTO request) {

        log.info("Saving answer for user: {}, question: {}", request.getUserId(), request.getQuestionId());

        User user = User.builder().id(request.getUserId()).build();

        CustomerAnswer answer = answerService.saveAnswer(
                user,
                request.getQuestionId(),
                request.getAnswerText()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toCustomerAnswerResponseDTO(answer));
    }

    /**
     * Get answer by ID
     * GET /api/v1/answers/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get answer by ID",
        description = "Retrieves a specific customer answer by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved answer",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Answer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerAnswerResponseDTO> getAnswer(
        @Parameter(description = "Answer ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("Fetching answer: {}", id);

        CustomerAnswer answer = answerService.getAnswerById(id);
        return ResponseEntity.ok(mapper.toCustomerAnswerResponseDTO(answer));
    }

    /**
     * Get all answers for user
     * GET /api/v1/answers/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get answers by user",
        description = "Retrieves all answers submitted by a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user answers",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<CustomerAnswerResponseDTO>> getAnswersByUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        log.info("Fetching answers for user: {}", userId);

        List<CustomerAnswer> answers = answerService.getAnswersByUser(userId);
        List<CustomerAnswerResponseDTO> response = answers.stream()
                .map(mapper::toCustomerAnswerResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get completed answers for user
     * GET /api/v1/answers/user/{userId}/completed
     */
    @GetMapping("/user/{userId}/completed")
    @Operation(
        summary = "Get completed answers by user",
        description = "Retrieves all completed answers submitted by a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved completed answers",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<CustomerAnswerResponseDTO>> getCompletedAnswers(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        log.info("Fetching completed answers for user: {}", userId);

        List<CustomerAnswer> answers = answerService.getCompletedAnswersByUser(userId);
        List<CustomerAnswerResponseDTO> response = answers.stream()
                .map(mapper::toCustomerAnswerResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get specific answer for user and question
     * GET /api/v1/answers/user/{userId}/question/{questionId}
     */
    @GetMapping("/user/{userId}/question/{questionId}")
    @Operation(
        summary = "Get answer by user and question",
        description = "Retrieves a specific answer for a given user and question combination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved answer",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Answer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerAnswerResponseDTO> getAnswerByUserAndQuestion(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Integer questionId) {

        log.info("Fetching answer for user: {}, question: {}", userId, questionId);

        Optional<CustomerAnswer> answer = answerService.getAnswerByUserAndQuestion(userId, questionId);

        return answer.map(a -> ResponseEntity.ok(mapper.toCustomerAnswerResponseDTO(a)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Check if user answered question
     * GET /api/v1/answers/user/{userId}/question/{questionId}/answered
     */
    @GetMapping("/user/{userId}/question/{questionId}/answered")
    @Operation(
        summary = "Check if user answered question",
        description = "Checks whether a user has answered a specific question"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully checked answer status",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Boolean> hasAnswered(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Integer questionId) {

        log.info("Checking if user {} answered question {}", userId, questionId);

        boolean answered = answerService.hasAnswered(userId, questionId);
        return ResponseEntity.ok(answered);
    }

    /**
     * Update answer
     * PUT /api/v1/answers/{id}
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update an answer",
        description = "Updates an existing customer answer with new answer text"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Answer updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerAnswerResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Answer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerAnswerResponseDTO> updateAnswer(
            @Parameter(description = "Answer ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated answer data", required = true)
            @RequestBody CustomerAnswerRequestDTO request) {

        log.info("Updating answer: {}", id);

        CustomerAnswer updated = answerService.updateAnswer(id, request.getAnswerText());
        return ResponseEntity.ok(mapper.toCustomerAnswerResponseDTO(updated));
    }

    /**
     * Delete answer
     * DELETE /api/v1/answers/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an answer",
        description = "Deletes a customer answer by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Answer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Answer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAnswer(
        @Parameter(description = "Answer ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        log.info("Deleting answer: {}", id);

        answerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all answers for user
     * DELETE /api/v1/answers/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    @Operation(
        summary = "Delete all answers for user",
        description = "Deletes all answers associated with a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "All user answers deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAllAnswersForUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        log.info("Deleting all answers for user: {}", userId);

        answerService.deleteAllAnswersForUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count answers for user
     * GET /api/v1/answers/user/{userId}/count
     */
    @GetMapping("/user/{userId}/count")
    @Operation(
        summary = "Count answers by user",
        description = "Returns the total number of answers submitted by a specific user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved answer count",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Long> countAnswersByUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        log.info("Counting answers for user: {}", userId);

        long count = answerService.countAnswersByUser(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get answer completion rate
     * GET /api/v1/answers/user/{userId}/completion-rate/{totalQuestions}
     */
    @GetMapping("/user/{userId}/completion-rate/{totalQuestions}")
    @Operation(
        summary = "Get answer completion rate",
        description = "Calculates the percentage of questions answered by the user out of total questions"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated completion rate",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Double.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Double> getCompletionRate(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "Total number of questions", required = true, example = "10")
            @PathVariable int totalQuestions) {

        log.info("Calculating completion rate for user: {}", userId);

        double rate = answerService.getAnswerCompletionRate(userId, totalQuestions);
        return ResponseEntity.ok(rate);
    }
}
