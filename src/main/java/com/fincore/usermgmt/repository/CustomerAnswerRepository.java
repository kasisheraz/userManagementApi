package com.fincore.usermgmt.repository;

import com.fincore.usermgmt.entity.CustomerAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomerAnswer entity
 * Provides database operations for customer answers to questionnaire
 */
@Repository
public interface CustomerAnswerRepository extends JpaRepository<CustomerAnswer, Long> {

    /**
     * Find answers by user ID
     */
    List<CustomerAnswer> findByUser_Id(Long userId);

    /**
     * Find answers by user ID with pagination
     */
    Page<CustomerAnswer> findByUser_Id(Long userId, Pageable pageable);

    /**
     * Find answer by question ID
     */
    List<CustomerAnswer> findByQuestion_QuestionId(Integer questionId);

    /**
     * Find answer by user ID and question ID
     */
    Optional<CustomerAnswer> findByUser_IdAndQuestion_QuestionId(Long userId, Integer questionId);

    /**
     * Count answers by user ID
     */
    @Query("SELECT COUNT(a) FROM CustomerAnswer a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Find all answers for a user with question details
     */
    @Query("SELECT a FROM CustomerAnswer a WHERE a.user.id = :userId ORDER BY a.answeredAt DESC")
    List<CustomerAnswer> findAnswersWithQuestionDetails(@Param("userId") Long userId);

    /**
     * Delete answers by user ID
     */
    @Query("DELETE FROM CustomerAnswer a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
