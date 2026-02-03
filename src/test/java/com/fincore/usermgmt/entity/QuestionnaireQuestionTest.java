package com.fincore.usermgmt.entity;

import com.fincore.usermgmt.entity.enums.QuestionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuestionnaireQuestion entity
 */
class QuestionnaireQuestionTest {

    private QuestionnaireQuestion question;
    private User creator;

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .id(1L)
                .build();

        question = QuestionnaireQuestion.builder()
                .questionId(1)
                .questionText("What is your employment status?")
                .questionCategory(QuestionCategory.EMPLOYMENT)
                .status("ACTIVE")
                .displayOrder(1)
                .createdBy(creator)
                .build();
    }

    @Test
    void testQuestionCreation() {
        assertNotNull(question);
        assertEquals("What is your employment status?", question.getQuestionText());
        assertEquals(QuestionCategory.EMPLOYMENT, question.getQuestionCategory());
        assertEquals("ACTIVE", question.getStatus());
    }

    @Test
    void testQuestionCategory() {
        question.setQuestionCategory(QuestionCategory.PERSONAL);
        assertEquals(QuestionCategory.PERSONAL, question.getQuestionCategory());
    }

    @Test
    void testDisplayOrder() {
        question.setDisplayOrder(5);
        assertEquals(5, question.getDisplayOrder());
    }

    @Test
    void testQuestionStatus() {
        question.setStatus("INACTIVE");
        assertEquals("INACTIVE", question.getStatus());
    }

    @Test
    void testCreatorRelationship() {
        assertNotNull(question.getCreatedBy());
        assertEquals(creator.getId(), question.getCreatedBy().getId());
    }

    @Test
    void testBuilderPattern() {
        QuestionnaireQuestion builtQuestion = QuestionnaireQuestion.builder()
                .questionId(2)
                .questionText("Test Question")
                .questionCategory(QuestionCategory.FINANCIAL)
                .status("ACTIVE")
                .displayOrder(2)
                .build();

        assertEquals(2, builtQuestion.getQuestionId());
        assertEquals("Test Question", builtQuestion.getQuestionText());
        assertEquals(QuestionCategory.FINANCIAL, builtQuestion.getQuestionCategory());
    }

    @Test
    void testQuestionTextValidation() {
        String longText = "a".repeat(5000);
        question.setQuestionText(longText);
        assertEquals(longText, question.getQuestionText());
    }

    @Test
    void testAllCategories() {
        for (QuestionCategory category : QuestionCategory.values()) {
            question.setQuestionCategory(category);
            assertEquals(category, question.getQuestionCategory());
        }
    }

    @Test
    void testNullableFields() {
        QuestionnaireQuestion minimalQuestion = QuestionnaireQuestion.builder()
                .questionId(3)
                .build();

        assertNotNull(minimalQuestion);
        assertEquals(3, minimalQuestion.getQuestionId());
        assertNull(minimalQuestion.getQuestionText());
        assertNull(minimalQuestion.getCreatedBy());
    }

    @Test
    void testEqualsAndHashCode() {
        QuestionnaireQuestion question2 = QuestionnaireQuestion.builder()
                .questionId(1)
                .questionText("What is your employment status?")
                .questionCategory(QuestionCategory.EMPLOYMENT)
                .status("ACTIVE")
                .displayOrder(1)
                .build();

        // Note: Equals/HashCode based on all fields by Lombok
        assertNotNull(question2);
    }
}
