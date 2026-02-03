package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.QuestionnaireQuestionRequestDTO;
import com.fincore.usermgmt.dto.QuestionnaireQuestionResponseDTO;
import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.QuestionCategory;
import com.fincore.usermgmt.service.QuestionnaireService;
import com.fincore.usermgmt.mapper.KycAmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Questionnaire endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final KycAmlMapper mapper;

    /**
     * Get all questions
     * GET /api/v1/questions
     */
    @GetMapping
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
    public ResponseEntity<QuestionnaireQuestionResponseDTO> getQuestion(@PathVariable Integer id) {
        log.info("Fetching question: {}", id);

        QuestionnaireQuestion question = questionnaireService.getQuestionById(id);
        return ResponseEntity.ok(mapper.toQuestionnaireQuestionResponseDTO(question));
    }

    /**
     * Get active questions
     * GET /api/v1/questions/active
     */
    @GetMapping("/active")
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
    public ResponseEntity<List<QuestionnaireQuestionResponseDTO>> getQuestionsByCategory(
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
     * POST /api/v1/questions
     */
    @PostMapping
    public ResponseEntity<QuestionnaireQuestionResponseDTO> createQuestion(
            @RequestBody QuestionnaireQuestionRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {

        log.info("Creating new question: {}", request.getQuestionText());

        User creator = userId != null ? User.builder().id(userId).build() : null;

        QuestionnaireQuestion question = questionnaireService.createQuestion(
                request.getQuestionText(),
                QuestionCategory.valueOf(request.getQuestionCategory()),
                request.getDisplayOrder(),
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
    public ResponseEntity<QuestionnaireQuestionResponseDTO> updateQuestion(
            @PathVariable Integer id,
            @RequestBody QuestionnaireQuestionRequestDTO request,
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
    public ResponseEntity<QuestionnaireQuestionResponseDTO> activateQuestion(
            @PathVariable Integer id,
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
    public ResponseEntity<QuestionnaireQuestionResponseDTO> inactivateQuestion(
            @PathVariable Integer id,
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
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer id) {
        log.info("Deleting question: {}", id);

        questionnaireService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get active questions count
     * GET /api/v1/questions/count/active
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> getActiveQuestionsCount() {
        log.info("Fetching active questions count");

        long count = questionnaireService.getActiveQuestionsCount();
        return ResponseEntity.ok(count);
    }
}
