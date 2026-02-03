package com.fincore.usermgmt.service;

import com.fincore.usermgmt.entity.CustomerAnswer;
import com.fincore.usermgmt.entity.QuestionnaireQuestion;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.repository.CustomerAnswerRepository;
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
 * Unit tests for CustomerAnswerService
 */
@ExtendWith(MockitoExtension.class)
public class CustomerAnswerServiceTest {

    @Mock
    private CustomerAnswerRepository answerRepository;

    @InjectMocks
    private CustomerAnswerService answerService;

    private User testUser;
    private QuestionnaireQuestion testQuestion;
    private CustomerAnswer testAnswer;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("customer@example.com")
                .build();

        testQuestion = QuestionnaireQuestion.builder()
                .questionId(1)
                .questionText("What is your occupation?")
                .build();

        testAnswer = CustomerAnswer.builder()
                .answerId(1L)
                .user(testUser)
                .question(testQuestion)
                .answerText("Software Engineer")
                .answeredAt(LocalDateTime.now())
                .build();
    }

    /**
     * Test save answer - new answer
     */
    @Test
    void testSaveAnswer_NewAnswer() {
        when(answerRepository.findByUserIdAndQuestionId(1L, 1))
                .thenReturn(Optional.empty());
        when(answerRepository.save(any(CustomerAnswer.class)))
                .thenReturn(testAnswer);

        CustomerAnswer result = answerService.saveAnswer(testUser, 1, "Software Engineer");

        assertNotNull(result);
        assertEquals("Software Engineer", result.getAnswerText());
        verify(answerRepository, times(1)).save(any());
    }

    /**
     * Test save answer - update existing answer
     */
    @Test
    void testSaveAnswer_UpdateAnswer() {
        CustomerAnswer existingAnswer = testAnswer;
        existingAnswer.setAnswerText("Updated answer");

        when(answerRepository.findByUserIdAndQuestionId(1L, 1))
                .thenReturn(Optional.of(testAnswer));
        when(answerRepository.save(any()))
                .thenReturn(existingAnswer);

        CustomerAnswer result = answerService.saveAnswer(testUser, 1, "Updated answer");

        assertEquals("Updated answer", result.getAnswerText());
        verify(answerRepository, times(1)).save(any());
    }

    /**
     * Test get answer by ID
     */
    @Test
    void testGetAnswerById_Success() {
        when(answerRepository.findById(1L))
                .thenReturn(Optional.of(testAnswer));

        CustomerAnswer result = answerService.getAnswerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAnswerId());
        verify(answerRepository, times(1)).findById(1L);
    }

    /**
     * Test get answer by ID not found
     */
    @Test
    void testGetAnswerById_NotFound() {
        when(answerRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> answerService.getAnswerById(999L));
    }

    /**
     * Test get answers by user
     */
    @Test
    void testGetAnswersByUser() {
        List<CustomerAnswer> answers = Arrays.asList(testAnswer);

        when(answerRepository.findByUserId(1L))
                .thenReturn(answers);

        List<CustomerAnswer> result = answerService.getAnswersByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(answerRepository, times(1)).findByUserId(1L);
    }

    /**
     * Test get answer by user and question
     */
    @Test
    void testGetAnswerByUserAndQuestion() {
        when(answerRepository.findByUserIdAndQuestionId(1L, 1))
                .thenReturn(Optional.of(testAnswer));

        Optional<CustomerAnswer> result = answerService.getAnswerByUserAndQuestion(1L, 1);

        assertTrue(result.isPresent());
        assertEquals(testAnswer.getAnswerId(), result.get().getAnswerId());
        verify(answerRepository, times(1)).findByUserIdAndQuestionId(1L, 1);
    }

    /**
     * Test count answers by user
     */
    @Test
    void testCountAnswersByUser() {
        when(answerRepository.countByUserId(1L))
                .thenReturn(5L);

        long count = answerService.countAnswersByUser(1L);

        assertEquals(5L, count);
        verify(answerRepository, times(1)).countByUserId(1L);
    }

    /**
     * Test update answer
     */
    @Test
    void testUpdateAnswer() {
        CustomerAnswer updated = testAnswer;
        updated.setAnswerText("Updated: Software Engineer");

        when(answerRepository.findById(1L))
                .thenReturn(Optional.of(testAnswer));
        when(answerRepository.save(any()))
                .thenReturn(updated);

        CustomerAnswer result = answerService.updateAnswer(1L, "Updated: Software Engineer");

        assertEquals("Updated: Software Engineer", result.getAnswerText());
        verify(answerRepository, times(1)).save(any());
    }

    /**
     * Test has required answers - all required answered
     */
    @Test
    void testHasRequiredAnswers_True() {
        List<CustomerAnswer> userAnswers = Arrays.asList(
                testAnswer,
                CustomerAnswer.builder()
                        .answerId(2L)
                        .user(testUser)
                        .question(QuestionnaireQuestion.builder().questionId(2).build())
                        .answerText("Answer 2")
                        .build()
        );

        when(answerRepository.findByUserId(1L))
                .thenReturn(userAnswers);

        boolean result = answerService.hasRequiredAnswers(1L, Arrays.asList(1, 2));

        assertTrue(result);
    }

    /**
     * Test has required answers - not all required answered
     */
    @Test
    void testHasRequiredAnswers_False() {
        List<CustomerAnswer> userAnswers = Arrays.asList(testAnswer);

        when(answerRepository.findByUserId(1L))
                .thenReturn(userAnswers);

        boolean result = answerService.hasRequiredAnswers(1L, Arrays.asList(1, 2, 3));

        assertFalse(result);
    }

    /**
     * Test get completed answers by user
     */
    @Test
    void testGetCompletedAnswersByUser() {
        List<CustomerAnswer> completedAnswers = Arrays.asList(testAnswer);

        when(answerRepository.findByUserIdAndAnswerTextIsNotNull(1L))
                .thenReturn(completedAnswers);

        List<CustomerAnswer> result = answerService.getCompletedAnswersByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(answerRepository, times(1)).findByUserIdAndAnswerTextIsNotNull(1L);
    }

    /**
     * Test delete answer
     */
    @Test
    void testDeleteAnswer() {
        doNothing().when(answerRepository).deleteById(1L);

        answerService.deleteAnswer(1L);

        verify(answerRepository, times(1)).deleteById(1L);
    }

    /**
     * Test delete all answers for user
     */
    @Test
    void testDeleteAllAnswersForUser() {
        doNothing().when(answerRepository).deleteByUserId(1L);

        answerService.deleteAllAnswersForUser(1L);

        verify(answerRepository, times(1)).deleteByUserId(1L);
    }

    /**
     * Test has answered - user answered question
     */
    @Test
    void testHasAnswered_True() {
        when(answerRepository.findByUserIdAndQuestionId(1L, 1))
                .thenReturn(Optional.of(testAnswer));

        boolean result = answerService.hasAnswered(1L, 1);

        assertTrue(result);
    }

    /**
     * Test has answered - user has not answered question
     */
    @Test
    void testHasAnswered_False() {
        when(answerRepository.findByUserIdAndQuestionId(anyLong(), anyInt()))
                .thenReturn(Optional.empty());

        boolean result = answerService.hasAnswered(1L, 999);

        assertFalse(result);
    }

    /**
     * Test get answer completion rate
     */
    @Test
    void testGetAnswerCompletionRate() {
        when(answerRepository.countByUserId(1L))
                .thenReturn(5L);

        double rate = answerService.getAnswerCompletionRate(1L, 10);

        assertEquals(50.0, rate);
    }

    /**
     * Test get answer completion rate - 100 percent
     */
    @Test
    void testGetAnswerCompletionRate_Full() {
        when(answerRepository.countByUserId(1L))
                .thenReturn(10L);

        double rate = answerService.getAnswerCompletionRate(1L, 10);

        assertEquals(100.0, rate);
    }

    /**
     * Test get answer completion rate - zero percent
     */
    @Test
    void testGetAnswerCompletionRate_Zero() {
        when(answerRepository.countByUserId(1L))
                .thenReturn(0L);

        double rate = answerService.getAnswerCompletionRate(1L, 10);

        assertEquals(0.0, rate);
    }

    /**
     * Test save answers bulk
     */
    @Test
    void testSaveAnswersBulk() {
        List<java.util.Map.Entry<Integer, String>> answers = Arrays.asList(
                new java.util.AbstractMap.SimpleEntry<>(1, "Answer 1"),
                new java.util.AbstractMap.SimpleEntry<>(2, "Answer 2")
        );

        when(answerRepository.save(any()))
                .thenReturn(testAnswer);

        answerService.saveAnswersBulk(testUser, answers);

        verify(answerRepository, times(2)).save(any());
    }

    /**
     * Test get answers by question
     */
    @Test
    void testGetAnswersByQuestion() {
        List<CustomerAnswer> questionAnswers = Arrays.asList(testAnswer);

        when(answerRepository.findByQuestionId(1))
                .thenReturn(questionAnswers);

        List<CustomerAnswer> result = answerService.getAnswersByQuestion(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
