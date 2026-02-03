package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.CustomerAnswerRequestDTO;
import com.fincore.usermgmt.dto.CustomerAnswerResponseDTO;
import com.fincore.usermgmt.entity.CustomerAnswer;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.service.CustomerAnswerService;
import com.fincore.usermgmt.mapper.KycAmlMapper;
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
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class CustomerAnswerController {

    private final CustomerAnswerService answerService;
    private final KycAmlMapper mapper;

    /**
     * Save or update answer
     * POST /api/v1/answers
     */
    @PostMapping
    public ResponseEntity<CustomerAnswerResponseDTO> saveAnswer(
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
    public ResponseEntity<CustomerAnswerResponseDTO> getAnswer(@PathVariable Long id) {
        log.info("Fetching answer: {}", id);

        CustomerAnswer answer = answerService.getAnswerById(id);
        return ResponseEntity.ok(mapper.toCustomerAnswerResponseDTO(answer));
    }

    /**
     * Get all answers for user
     * GET /api/v1/answers/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CustomerAnswerResponseDTO>> getAnswersByUser(@PathVariable Long userId) {
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
    public ResponseEntity<List<CustomerAnswerResponseDTO>> getCompletedAnswers(@PathVariable Long userId) {
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
    public ResponseEntity<CustomerAnswerResponseDTO> getAnswerByUserAndQuestion(
            @PathVariable Long userId,
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
    public ResponseEntity<Boolean> hasAnswered(
            @PathVariable Long userId,
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
    public ResponseEntity<CustomerAnswerResponseDTO> updateAnswer(
            @PathVariable Long id,
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
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        log.info("Deleting answer: {}", id);

        answerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all answers for user
     * DELETE /api/v1/answers/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteAllAnswersForUser(@PathVariable Long userId) {
        log.info("Deleting all answers for user: {}", userId);

        answerService.deleteAllAnswersForUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Count answers for user
     * GET /api/v1/answers/user/{userId}/count
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countAnswersByUser(@PathVariable Long userId) {
        log.info("Counting answers for user: {}", userId);

        long count = answerService.countAnswersByUser(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get answer completion rate
     * GET /api/v1/answers/user/{userId}/completion-rate/{totalQuestions}
     */
    @GetMapping("/user/{userId}/completion-rate/{totalQuestions}")
    public ResponseEntity<Double> getCompletionRate(
            @PathVariable Long userId,
            @PathVariable int totalQuestions) {

        log.info("Calculating completion rate for user: {}", userId);

        double rate = answerService.getAnswerCompletionRate(userId, totalQuestions);
        return ResponseEntity.ok(rate);
    }
}
