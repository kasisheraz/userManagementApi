package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.enums.QuestionCategory;
import com.fincore.usermgmt.repository.QuestionnaireQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuestionnaireService
 */
@ExtendWith(MockitoExtension.class)
public class QuestionnaireServiceTest {

    @Mock
    private QuestionnaireQuestionRepository questionnaireRepository;

    @InjectMocks
    private QuestionnaireService questionnaireService;

    private User testUser;
    private QuestionnaireQuestion testQuestion;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .build();

        testQuestion = QuestionnaireQuestion.builder()
                .questionId(1)
                .questionText("What is your occupation?")
                .questionCategory(QuestionCategory.OCCUPATION)
                .status("ACTIVE")
                .displayOrder(1)
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Test create question successfully
     */
    @Test
    void testCreateQuestion_Success() {
        when(questionnaireRepository.save(any(QuestionnaireQuestion.class)))
                .thenReturn(testQuestion);

        QuestionnaireQuestion result = questionnaireService.createQuestion(
                "What is your occupation?",
                QuestionCategory.OCCUPATION,
                1,
                testUser
        );

        assertNotNull(result);
        assertEquals("What is your occupation?", result.getQuestionText());
        assertEquals(QuestionCategory.OCCUPATION, result.getQuestionCategory());
        assertEquals("ACTIVE", result.getStatus());
        verify(questionnaireRepository, times(1)).save(any());
    }

    /**
     * Test get question by ID
     */
    @Test
    void testGetQuestionById_Success() {
        when(questionnaireRepository.findById(1))
                .thenReturn(Optional.of(testQuestion));

        QuestionnaireQuestion result = questionnaireService.getQuestionById(1);

        assertNotNull(result);
        assertEquals(1, result.getQuestionId());
        verify(questionnaireRepository, times(1)).findById(1);
    }

    /**
     * Test get question by ID not found
     */
    @Test
    void testGetQuestionById_NotFound() {
        when(questionnaireRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> questionnaireService.getQuestionById(999));
    }

    /**
     * Test get all questions
     */
    @Test
    void testGetAllQuestions() {
        List<QuestionnaireQuestion> questions = Arrays.asList(testQuestion);

        when(questionnaireRepository.findAll())
                .thenReturn(questions);

        List<QuestionnaireQuestion> result = questionnaireService.getAllQuestions();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(questionnaireRepository, times(1)).findAll();
    }

    /**
     * Test get active questions
     */
    @Test
    void testGetActiveQuestions() {
        List<QuestionnaireQuestion> activeQuestions = Arrays.asList(testQuestion);

        when(questionnaireRepository.findByStatus("ACTIVE"))
                .thenReturn(activeQuestions);

        List<QuestionnaireQuestion> result = questionnaireService.getActiveQuestions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(q -> "ACTIVE".equals(q.getStatus())));
    }

    /**
     * Test get questions by category
     */
    @Test
    void testGetQuestionsByCategory() {
        List<QuestionnaireQuestion> categoryQuestions = Arrays.asList(testQuestion);

        when(questionnaireRepository.findByQuestionCategory(QuestionCategory.OCCUPATION))
                .thenReturn(categoryQuestions);

        List<QuestionnaireQuestion> result = questionnaireService
                .getQuestionsByCategory(QuestionCategory.OCCUPATION);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream()
                .allMatch(q -> QuestionCategory.OCCUPATION.equals(q.getQuestionCategory())));
    }

    /**
     * Test get active questions by category
     */
    @Test
    void testGetActiveQuestionsByCategory() {
        List<QuestionnaireQuestion> activeByCategory = Arrays.asList(testQuestion);

        when(questionnaireRepository.findByStatusAndQuestionCategory("ACTIVE", QuestionCategory.OCCUPATION))
                .thenReturn(activeByCategory);

        List<QuestionnaireQuestion> result = questionnaireService
                .getActiveQuestionsByCategory(QuestionCategory.OCCUPATION);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream()
                .allMatch(q -> "ACTIVE".equals(q.getStatus())
                        && QuestionCategory.OCCUPATION.equals(q.getQuestionCategory())));
    }

    /**
     * Test activate question
     */
    @Test
    void testActivateQuestion() {
        QuestionnaireQuestion inactiveQuestion = testQuestion;
        inactiveQuestion.setStatus("INACTIVE");

        QuestionnaireQuestion activeQuestion = testQuestion;
        activeQuestion.setStatus("ACTIVE");

        when(questionnaireRepository.findById(1))
                .thenReturn(Optional.of(inactiveQuestion));
        when(questionnaireRepository.save(any()))
                .thenReturn(activeQuestion);

        QuestionnaireQuestion result = questionnaireService.activateQuestion(1, testUser);

        assertEquals("ACTIVE", result.getStatus());
        verify(questionnaireRepository, times(1)).save(any());
    }

    /**
     * Test inactivate question
     */
    @Test
    void testInactivateQuestion() {
        QuestionnaireQuestion activeQuestion = testQuestion;
        activeQuestion.setStatus("ACTIVE");

        QuestionnaireQuestion inactiveQuestion = testQuestion;
        inactiveQuestion.setStatus("INACTIVE");

        when(questionnaireRepository.findById(1))
                .thenReturn(Optional.of(activeQuestion));
        when(questionnaireRepository.save(any()))
                .thenReturn(inactiveQuestion);

        QuestionnaireQuestion result = questionnaireService.inactivateQuestion(1, testUser);

        assertEquals("INACTIVE", result.getStatus());
        verify(questionnaireRepository, times(1)).save(any());
    }

    /**
     * Test archive question
     */
    @Test
    void testArchiveQuestion() {
        QuestionnaireQuestion activeQuestion = testQuestion;
        activeQuestion.setStatus("ACTIVE");

        QuestionnaireQuestion archivedQuestion = testQuestion;
        archivedQuestion.setStatus("ARCHIVED");

        when(questionnaireRepository.findById(1))
                .thenReturn(Optional.of(activeQuestion));
        when(questionnaireRepository.save(any()))
                .thenReturn(archivedQuestion);

        QuestionnaireQuestion result = questionnaireService.archiveQuestion(1, testUser);

        assertEquals("ARCHIVED", result.getStatus());
        verify(questionnaireRepository, times(1)).save(any());
    }

    /**
     * Test update question
     */
    @Test
    void testUpdateQuestion() {
        QuestionnaireQuestion updated = testQuestion;
        updated.setQuestionText("Updated question text?");
        updated.setQuestionCategory(QuestionCategory.INCOME);

        when(questionnaireRepository.findById(1))
                .thenReturn(Optional.of(testQuestion));
        when(questionnaireRepository.save(any()))
                .thenReturn(updated);

        QuestionnaireQuestion result = questionnaireService.updateQuestion(
                1,
                "Updated question text?",
                QuestionCategory.INCOME,
                null,
                1,
                testUser
        );

        assertEquals("Updated question text?", result.getQuestionText());
        assertEquals(QuestionCategory.INCOME, result.getQuestionCategory());
        verify(questionnaireRepository, times(1)).save(any());
    }

    /**
     * Test update display order
     */
    @Test
    void testUpdateDisplayOrder() {
        QuestionnaireQuestion withNewOrder = testQuestion;
        withNewOrder.setDisplayOrder(5);

        when(questionnaireRepository.findById(1))
                .thenReturn(Optional.of(testQuestion));
        when(questionnaireRepository.save(any()))
                .thenReturn(withNewOrder);

        QuestionnaireQuestion result = questionnaireService.updateDisplayOrder(1, 5, testUser);

        assertEquals(5, result.getDisplayOrder());
        verify(questionnaireRepository, times(1)).save(any());
    }

    /**
     * Test count by status
     */
    @Test
    void testCountByStatus() {
        when(questionnaireRepository.countByStatus("ACTIVE"))
                .thenReturn(10L);

        long count = questionnaireService.countByStatus("ACTIVE");

        assertEquals(10L, count);
        verify(questionnaireRepository, times(1)).countByStatus("ACTIVE");
    }

    /**
     * Test get active questions count
     */
    @Test
    void testGetActiveQuestionsCount() {
        when(questionnaireRepository.countByStatus("ACTIVE"))
                .thenReturn(15L);

        long count = questionnaireService.getActiveQuestionsCount();

        assertEquals(15L, count);
    }

    /**
     * Test delete question
     */
    @Test
    void testDeleteQuestion() {
        doNothing().when(questionnaireRepository).deleteById(1);

        questionnaireService.deleteQuestion(1);

        verify(questionnaireRepository, times(1)).deleteById(1);
    }

    /**
     * Test find question by ID returns Optional
     */
    @Test
    void testFindQuestionById() {
        when(questionnaireRepository.findById(1))
                .thenReturn(Optional.of(testQuestion));

        Optional<QuestionnaireQuestion> result = questionnaireService.findQuestionById(1);

        assertTrue(result.isPresent());
        assertEquals(testQuestion.getQuestionId(), result.get().getQuestionId());
    }

    /**
     * Test get questions by status
     */
    @Test
    void testGetQuestionsByStatus() {
        List<QuestionnaireQuestion> byStatus = Arrays.asList(testQuestion);

        when(questionnaireRepository.findByStatus("ACTIVE"))
                .thenReturn(byStatus);

        List<QuestionnaireQuestion> result = questionnaireService.getQuestionsByStatus("ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test get questions by category and status
     */
    @Test
    void testGetQuestionsByCategoryAndStatus() {
        List<QuestionnaireQuestion> filtered = Arrays.asList(testQuestion);

        when(questionnaireRepository.findByStatusAndQuestionCategory("ACTIVE", QuestionCategory.OCCUPATION))
                .thenReturn(filtered);

        List<QuestionnaireQuestion> result = questionnaireService
                .getQuestionsByCategoryAndStatus(QuestionCategory.OCCUPATION, "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test reorder questions
     */
    @Test
    void testReorderQuestions() {
        List<Integer> questionIds = Arrays.asList(1, 2, 3);
        
        when(questionnaireRepository.findById(anyInt()))
                .thenReturn(Optional.of(testQuestion));
        when(questionnaireRepository.save(any()))
                .thenReturn(testQuestion);

        questionnaireService.reorderQuestions(QuestionCategory.OCCUPATION, questionIds, testUser);

        verify(questionnaireRepository, atLeast(1)).save(any());
    }
}
