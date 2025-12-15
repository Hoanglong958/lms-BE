package com.ra.base_spring_boot.config.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.dto.QuizResult.QuizResultResponseDTO;
import com.ra.base_spring_boot.dto.QuizResult.QuizSubmissionRequestDTO;
import com.ra.base_spring_boot.services.IQuizResultService;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.security.jwt.JwtTokenFilter;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = QuizResultController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("removal")
class QuizResultControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private IQuizResultService quizResultService;
    @MockBean
    private MyUserDetailsService myUserDetailsService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private JwtTokenFilter jwtTokenFilter;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void submitQuiz_shouldReturnResponse() throws Exception {
        QuizResultResponseDTO responseDTO = new QuizResultResponseDTO();
        responseDTO.setId(5L);
        responseDTO.setQuizId(1L);
        responseDTO.setUserId(2L);
        responseDTO.setCorrectCount(4);
        responseDTO.setTotalCount(5);
        responseDTO.setScore(80);
        responseDTO.setIsPassed(true);
        responseDTO.setSubmittedAt(LocalDateTime.now());

        when(quizResultService.submitQuiz(any(QuizSubmissionRequestDTO.class))).thenReturn(responseDTO);

        QuizSubmissionRequestDTO request = QuizSubmissionRequestDTO.builder()
                .quizId(1L)
                .userId(2L)
                .answers(List.of(
                        QuizSubmissionRequestDTO.AnswerItem.builder().questionId(10L).answer("A").build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/quiz-results/submit")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON, "mediaType must not be null"))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsBytes(request), "body must not be null")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.score").value(80));

        verify(quizResultService).submitQuiz(any(QuizSubmissionRequestDTO.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void submitQuiz_shouldFailValidation() throws Exception {
        QuizSubmissionRequestDTO invalidRequest = QuizSubmissionRequestDTO.builder()
                .quizId(null)
                .userId(2L)
                .answers(List.of())
                .build();

        mockMvc.perform(post("/api/v1/quiz-results/submit")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON, "mediaType must not be null"))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsBytes(invalidRequest), "body must not be null")))
                .andExpect(status().isBadRequest());
    }
}

