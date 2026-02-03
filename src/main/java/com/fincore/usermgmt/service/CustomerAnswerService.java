package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.CustomerAnswer;
import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.repository.CustomerAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing customer questionnaire answers
 * Handles answer creation, validation, retrieval, and management
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerAnswerService {

    private final CustomerAnswerRepository answerRepository;
    private final QuestionnaireService questionnaireService;

    /**
     * Save or update customer answer
     */
    public CustomerAnswer saveAnswer(
            User user,
            Integer questionId,
            String answerText) {

        log.info("Saving answer for user: {}, question: {}", user.getId(), questionId);

        // Validate question exists
        QuestionnaireQuestion question = questionnaireService.getQuestionById(questionId);

        // Check for existing answer
        Optional<CustomerAnswer> existingAnswer = answerRepository
                .findByUser_IdAndQuestion_QuestionId(user.getId(), questionId);

        CustomerAnswer answer;
        if (existingAnswer.isPresent()) {
            log.info("Updating existing answer for user: {}, question: {}", user.getId(), questionId);
            answer = existingAnswer.get();
            answer.setAnswerText(answerText);
            answer.setAnsweredAt(LocalDateTime.now());
        } else {
            answer = CustomerAnswer.builder()
                    .user(user)
                    .question(question)
                    .answerText(answerText)
                    .answeredAt(LocalDateTime.now())
                    .build();
        }

        CustomerAnswer saved = answerRepository.save(answer);
        log.info("Answer saved with ID: {}", saved.getAnswerId());
        return saved;
    }

    /**
     * Get answer by ID
     */
    @Transactional(readOnly = true)
    public CustomerAnswer getAnswerById(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
    }

    /**
     * Get all answers for user
     */
    @Transactional(readOnly = true)
    public List<CustomerAnswer> getAnswersByUser(Long userId) {
        log.info("Fetching answers for user: {}", userId);
        return answerRepository.findByUser_Id(userId);
    }

    /**
     * Get user answers with pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerAnswer> getAnswersByUserPaged(Long userId, Pageable pageable) {
        return answerRepository.findByUser_Id(userId, pageable);
    }

    /**
     * Get all answers for a specific question
     */
    @Transactional(readOnly = true)
    public List<CustomerAnswer> getAnswersByQuestion(Integer questionId) {
        log.info("Fetching answers for question: {}", questionId);
        return answerRepository.findByQuestion_QuestionId(questionId);
    }

    /**
     * Get specific answer for user and question
     */
    @Transactional(readOnly = true)
    public Optional<CustomerAnswer> getAnswerByUserAndQuestion(Long userId, Integer questionId) {
        return answerRepository.findByUser_IdAndQuestion_QuestionId(userId, questionId);
    }

    /**
     * Count answers for user
     */
    @Transactional(readOnly = true)
    public long countAnswersByUser(Long userId) {
        return answerRepository.countByUserId(userId);
    }

    /**
     * Update answer text
     */
    public CustomerAnswer updateAnswer(Long answerId, String newAnswerText) {
        log.info("Updating answer: {}", answerId);

        CustomerAnswer answer = getAnswerById(answerId);
        answer.setAnswerText(newAnswerText);
        answer.setAnsweredAt(LocalDateTime.now());

        return answerRepository.save(answer);
    }

    /**
     * Validate all required answers are provided
     */
    @Transactional(readOnly = true)
    public boolean hasRequiredAnswers(Long userId, List<Integer> requiredQuestionIds) {
        log.info("Validating required answers for user: {}", userId);

        List<CustomerAnswer> userAnswers = getAnswersByUser(userId);
        List<Integer> answeredQuestionIds = userAnswers.stream()
                .map(a -> a.getQuestion().getQuestionId())
                .toList();

        return answeredQuestionIds.containsAll(requiredQuestionIds);
    }

    /**
     * Get answers with non-empty text for user
     */
    @Transactional(readOnly = true)
    public List<CustomerAnswer> getCompletedAnswersByUser(Long userId) {
        List<CustomerAnswer> answers = getAnswersByUser(userId);
        return answers.stream()
                .filter(a -> a.getAnswerText() != null && !a.getAnswerText().trim().isEmpty())
                .toList();
    }

    /**
     * Delete answer
     */
    public void deleteAnswer(Long answerId) {
        log.info("Deleting answer: {}", answerId);
        answerRepository.deleteById(answerId);
    }

    /**
     * Delete all answers for user
     */
    public void deleteAllAnswersForUser(Long userId) {
        log.info("Deleting all answers for user: {}", userId);
        answerRepository.deleteByUserId(userId);
    }

    /**
     * Check if user answered question
     */
    @Transactional(readOnly = true)
    public boolean hasAnswered(Long userId, Integer questionId) {
        return getAnswerByUserAndQuestion(userId, questionId).isPresent();
    }

    /**
     * Get answer completion rate for user
     */
    @Transactional(readOnly = true)
    public double getAnswerCompletionRate(Long userId, int totalQuestions) {
        long answeredCount = getCompletedAnswersByUser(userId).size();
        return (double) answeredCount / totalQuestions * 100;
    }

    /**
     * Bulk save answers
     */
    public List<CustomerAnswer> saveAnswersBulk(
            User user,
            List<Map.Entry<Integer, String>> questionAnswerPairs) {

        log.info("Saving {} answers for user: {}", questionAnswerPairs.size(), user.getId());

        return questionAnswerPairs.stream()
                .map(pair -> saveAnswer(user, pair.getKey(), pair.getValue()))
                .toList();
    }
}
