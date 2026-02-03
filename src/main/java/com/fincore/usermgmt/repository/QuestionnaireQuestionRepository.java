package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.enums.QuestionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for QuestionnaireQuestion entity
 * Provides database operations for questionnaire questions
 */
@Repository
public interface QuestionnaireQuestionRepository extends JpaRepository<QuestionnaireQuestion, Integer> {

    /**
     * Find questions by status
     */
    List<QuestionnaireQuestion> findByStatus(String status);

    /**
     * Find questions by status with pagination
     */
    Page<QuestionnaireQuestion> findByStatus(String status, Pageable pageable);

    /**
     * Find questions by category
     */
    List<QuestionnaireQuestion> findByQuestionCategory(QuestionCategory category);

    /**
     * Find questions by category with pagination
     */
    Page<QuestionnaireQuestion> findByQuestionCategory(QuestionCategory category, Pageable pageable);

    /**
     * Find questions by category and status
     */
    @Query("SELECT q FROM QuestionnaireQuestion q WHERE q.questionCategory = :category AND q.status = :status ORDER BY q.displayOrder ASC")
    List<QuestionnaireQuestion> findByCategoryAndStatus(@Param("category") QuestionCategory category, @Param("status") String status);

    /**
     * Find questions by category and status with pagination
     */
    @Query("SELECT q FROM QuestionnaireQuestion q WHERE q.questionCategory = :category AND q.status = :status ORDER BY q.displayOrder ASC")
    Page<QuestionnaireQuestion> findByCategoryAndStatus(@Param("category") QuestionCategory category, @Param("status") String status, Pageable pageable);

    /**
     * Find all active questions ordered by display order
     */
    @Query("SELECT q FROM QuestionnaireQuestion q WHERE q.status = 'ACTIVE' ORDER BY q.displayOrder ASC")
    List<QuestionnaireQuestion> findAllActiveQuestions();

    /**
     * Find all active questions by category
     */
    @Query("SELECT q FROM QuestionnaireQuestion q WHERE q.status = 'ACTIVE' AND q.questionCategory = :category ORDER BY q.displayOrder ASC")
    List<QuestionnaireQuestion> findAllActiveQuestionsByCategory(@Param("category") QuestionCategory category);

    /**
     * Count questions by status
     */
    @Query("SELECT COUNT(q) FROM QuestionnaireQuestion q WHERE q.status = :status")
    long countByStatus(@Param("status") String status);
}
