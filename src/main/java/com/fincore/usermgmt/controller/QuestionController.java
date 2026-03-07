package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.QuestionnaireQuestionResponseDTO;
import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.mapper.KycAmlMapper;
import com.fincore.usermgmt.service.QuestionnaireService;
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
public class QuestionController {

    private final QuestionnaireService questionnaireService;
    private final KycAmlMapper mapper;

    /**
     * Get all questions
     * GET /api/questions
     */
    @GetMapping
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
    public ResponseEntity<QuestionnaireQuestionResponseDTO> getQuestion(@PathVariable Integer id) {
        log.info("Fetching question {} via /api/questions endpoint", id);

        QuestionnaireQuestion question = questionnaireService.getQuestionById(id);
        return ResponseEntity.ok(mapper.toQuestionnaireQuestionResponseDTO(question));
    }

    /**
     * Get active questions
     * GET /api/questions/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<QuestionnaireQuestionResponseDTO>> getActiveQuestions() {
        log.info("Fetching active questions via /api/questions endpoint");

        List<QuestionnaireQuestion> questions = questionnaireService.getActiveQuestions();
        List<QuestionnaireQuestionResponseDTO> response = questions.stream()
                .map(mapper::toQuestionnaireQuestionResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
