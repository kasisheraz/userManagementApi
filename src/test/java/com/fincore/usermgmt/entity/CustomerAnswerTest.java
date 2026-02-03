package com.fincore.usermgmt.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CustomerAnswer entity
 */
class CustomerAnswerTest {

    private CustomerAnswer answer;
    private User testUser;
    private QuestionnaireQuestion question;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .build();

        question = QuestionnaireQuestion.builder()
                .questionId(1)
                .questionText("What is your employment status?")
                .build();

        answer = CustomerAnswer.builder()
                .user(testUser)
                .question(question)
                .answerText("Employed - Full time")
                .build();
    }

    @Test
    void testAnswerCreation() {
        assertNotNull(answer);
        assertEquals(testUser.getId(), answer.getUser().getId());
        assertEquals(question.getQuestionId(), answer.getQuestion().getQuestionId());
        assertEquals("Employed - Full time", answer.getAnswerText());
    }

    @Test
    void testAnswerText() {
        answer.setAnswerText("Self-employed");
        assertEquals("Self-employed", answer.getAnswerText());
    }

    @Test
    void testAnswerTextLength() {
        String longAnswer = "a".repeat(500);
        answer.setAnswerText(longAnswer);
        assertEquals(longAnswer, answer.getAnswerText());
    }

    @Test
    void testAnsweredAt() {
        LocalDateTime now = LocalDateTime.now();
        answer.setAnsweredAt(now);
        assertEquals(now, answer.getAnsweredAt());
    }

    @Test
    void testUserRelationship() {
        assertNotNull(answer.getUser());
        assertEquals(testUser.getId(), answer.getUser().getId());
    }

    @Test
    void testQuestionRelationship() {
        assertNotNull(answer.getQuestion());
        assertEquals(question.getQuestionId(), answer.getQuestion().getQuestionId());
    }

    @Test
    void testBuilderPattern() {
        CustomerAnswer builtAnswer = CustomerAnswer.builder()
                .user(testUser)
                .question(question)
                .answerText("Unemployed")
                .build();

        assertEquals(testUser.getId(), builtAnswer.getUser().getId());
        assertEquals(question.getQuestionId(), builtAnswer.getQuestion().getQuestionId());
        assertEquals("Unemployed", builtAnswer.getAnswerText());
    }

    @Test
    void testAuditFields() {
        User auditor = User.builder().id(2L).build();
        answer.setCreatedBy(auditor);
        answer.setLastModifiedBy(auditor);

        assertNotNull(answer.getCreatedBy());
        assertNotNull(answer.getLastModifiedBy());
    }

    @Test
    void testAnswerId() {
        answer.setAnswerId(100L);
        assertEquals(100L, answer.getAnswerId());
    }

    @Test
    void testNullableFields() {
        CustomerAnswer minimalAnswer = CustomerAnswer.builder()
                .user(testUser)
                .question(question)
                .build();

        assertNotNull(minimalAnswer);
        assertNull(minimalAnswer.getAnswerText());
        assertNull(minimalAnswer.getAnsweredAt());
    }

    @Test
    void testMultipleAnswersPerQuestion() {
        User user2 = User.builder().id(2L).build();
        
        CustomerAnswer answer1 = CustomerAnswer.builder()
                .user(testUser)
                .question(question)
                .answerText("Answer 1")
                .build();

        CustomerAnswer answer2 = CustomerAnswer.builder()
                .user(user2)
                .question(question)
                .answerText("Answer 2")
                .build();

        assertNotNull(answer1);
        assertNotNull(answer2);
        assertEquals(question.getQuestionId(), answer1.getQuestion().getQuestionId());
        assertEquals(question.getQuestionId(), answer2.getQuestion().getQuestionId());
    }

    @Test
    void testModificationTracking() {
        LocalDateTime created = LocalDateTime.now().minusHours(1);
        LocalDateTime modified = LocalDateTime.now();

        answer.setCreatedDatetime(created);
        answer.setLastModifiedDatetime(modified);

        assertNotNull(answer.getCreatedDatetime());
        assertNotNull(answer.getLastModifiedDatetime());
        assertTrue(answer.getLastModifiedDatetime().isAfter(answer.getCreatedDatetime()));
    }
}
