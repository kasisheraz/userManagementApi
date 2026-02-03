package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing customer answers to questionnaire questions
 * Tracks customer responses for KYC verification purposes
 */
@Entity
@Table(name = "customer_answers", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_question_id", columnList = "question_id"),
    @Index(name = "idx_user_question", columnList = "user_id, question_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionnaireQuestion question;

    @Column(name = "answer", nullable = false, length = 500)
    private String answer;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @CreationTimestamp
    @Column(name = "created_datetime", nullable = false, updatable = false)
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @UpdateTimestamp
    @Column(name = "last_modified_datetime")
    private LocalDateTime lastModifiedDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    @PrePersist
    protected void onCreate() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
        if (createdDatetime == null) {
            createdDatetime = LocalDateTime.now();
        }
        if (lastModifiedDatetime == null) {
            lastModifiedDatetime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDatetime = LocalDateTime.now();
    }
}
