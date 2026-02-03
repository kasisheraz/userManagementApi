package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.QuestionCategory;
import com.fincore.usermgmt.repository.QuestionnaireQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing questionnaire questions
 * Handles question CRUD operations, categorization, and ordering
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionnaireService {

    private final QuestionnaireQuestionRepository questionRepository;

    /**
     * Create a new questionnaire question
     */
    public QuestionnaireQuestion createQuestion(
            String questionText,
            QuestionCategory category,
            Integer displayOrder,
            User creator) {

        log.info("Creating questionnaire question: {}", questionText);

        QuestionnaireQuestion question = QuestionnaireQuestion.builder()
                .questionText(questionText)
                .questionCategory(category)
                .status("ACTIVE")
                .displayOrder(displayOrder)
                .createdBy(creator)
                .build();

        QuestionnaireQuestion saved = questionRepository.save(question);
        log.info("Question created with ID: {}", saved.getQuestionId());
        return saved;
    }

    /**
     * Get question by ID
     */
    @Transactional(readOnly = true)
    public QuestionnaireQuestion getQuestionById(Integer questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
    }

    /**
     * Get all questions
     */
    @Transactional(readOnly = true)
    public List<QuestionnaireQuestion> getAllQuestions() {
        log.info("Fetching all questions");
        return questionRepository.findAll();
    }

    /**
     * Get all questions with pagination
     */
    @Transactional(readOnly = true)
    public Page<QuestionnaireQuestion> getAllQuestionsPaged(Pageable pageable) {
        return questionRepository.findAll(pageable);
    }

    /**
     * Get all active questions
     */
    @Transactional(readOnly = true)
    public List<QuestionnaireQuestion> getActiveQuestions() {
        log.info("Fetching active questions");
        return questionRepository.findAllActiveQuestions();
    }

    /**
     * Get questions by status
     */
    @Transactional(readOnly = true)
    public List<QuestionnaireQuestion> getQuestionsByStatus(String status) {
        log.info("Fetching questions with status: {}", status);
        return questionRepository.findByStatus(status);
    }

    /**
     * Get questions by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<QuestionnaireQuestion> getQuestionsByStatusPaged(String status, Pageable pageable) {
        return questionRepository.findByStatus(status, pageable);
    }

    /**
     * Get questions by category
     */
    @Transactional(readOnly = true)
    public List<QuestionnaireQuestion> getQuestionsByCategory(QuestionCategory category) {
        log.info("Fetching questions with category: {}", category);
        return questionRepository.findByQuestionCategory(category);
    }

    /**
     * Get questions by category with pagination
     */
    @Transactional(readOnly = true)
    public Page<QuestionnaireQuestion> getQuestionsByCategoryPaged(
            QuestionCategory category,
            Pageable pageable) {
        return questionRepository.findByQuestionCategory(category, pageable);
    }

    /**
     * Get active questions by category
     */
    @Transactional(readOnly = true)
    public List<QuestionnaireQuestion> getActiveQuestionsByCategory(QuestionCategory category) {
        log.info("Fetching active questions with category: {}", category);
        return questionRepository.findAllActiveQuestionsByCategory(category);
    }

    /**
     * Get questions by category and status
     */
    @Transactional(readOnly = true)
    public List<QuestionnaireQuestion> getQuestionsByCategoryAndStatus(
            QuestionCategory category,
            String status) {
        log.info("Fetching questions with category: {}, status: {}", category, status);
        return questionRepository.findByCategoryAndStatus(category, status);
    }

    /**
     * Get questions by category and status with pagination
     */
    @Transactional(readOnly = true)
    public Page<QuestionnaireQuestion> getQuestionsByCategoryAndStatusPaged(
            QuestionCategory category,
            String status,
            Pageable pageable) {
        return questionRepository.findByCategoryAndStatus(category, status, pageable);
    }

    /**
     * Update questionnaire question
     */
    public QuestionnaireQuestion updateQuestion(
            Integer questionId,
            String questionText,
            QuestionCategory category,
            String status,
            Integer displayOrder,
            User lastModifiedBy) {

        log.info("Updating question: {}", questionId);

        QuestionnaireQuestion question = getQuestionById(questionId);

        if (questionText != null) {
            question.setQuestionText(questionText);
        }
        if (category != null) {
            question.setQuestionCategory(category);
        }
        if (status != null) {
            question.setStatus(status);
        }
        if (displayOrder != null) {
            question.setDisplayOrder(displayOrder);
        }

        question.setLastModifiedBy(lastModifiedBy);

        return questionRepository.save(question);
    }

    /**
     * Activate a question
     */
    public QuestionnaireQuestion activateQuestion(Integer questionId, User modifiedBy) {
        return updateQuestion(questionId, null, null, "ACTIVE", null, modifiedBy);
    }

    /**
     * Inactivate a question
     */
    public QuestionnaireQuestion inactivateQuestion(Integer questionId, User modifiedBy) {
        return updateQuestion(questionId, null, null, "INACTIVE", null, modifiedBy);
    }

    /**
     * Archive a question
     */
    public QuestionnaireQuestion archiveQuestion(Integer questionId, User modifiedBy) {
        return updateQuestion(questionId, null, null, "ARCHIVED", null, modifiedBy);
    }

    /**
     * Update display order
     */
    public QuestionnaireQuestion updateDisplayOrder(
            Integer questionId,
            Integer displayOrder,
            User modifiedBy) {
        return updateQuestion(questionId, null, null, null, displayOrder, modifiedBy);
    }

    /**
     * Count questions by status
     */
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return questionRepository.countByStatus(status);
    }

    /**
     * Reorder questions by category
     */
    public void reorderQuestions(QuestionCategory category, List<Integer> questionIds, User modifiedBy) {
        log.info("Reordering {} questions in category: {}", questionIds.size(), category);

        for (int i = 0; i < questionIds.size(); i++) {
            updateDisplayOrder(questionIds.get(i), i + 1, modifiedBy);
        }
    }

    /**
     * Delete question
     */
    public void deleteQuestion(Integer questionId) {
        log.info("Deleting question: {}", questionId);
        questionRepository.deleteById(questionId);
    }

    /**
     * Get total active questions count
     */
    @Transactional(readOnly = true)
    public long getActiveQuestionsCount() {
        return countByStatus("ACTIVE");
    }

    /**
     * Get question by ID optionally
     */
    @Transactional(readOnly = true)
    public Optional<QuestionnaireQuestion> findQuestionById(Integer questionId) {
        return questionRepository.findById(questionId);
    }
}
