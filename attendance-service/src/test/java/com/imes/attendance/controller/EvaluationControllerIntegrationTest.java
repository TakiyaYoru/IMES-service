package com.imes.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imes.attendance.exception.GlobalExceptionHandler;
import com.imes.common.dto.request.CreateEvaluationRequest;
import com.imes.common.dto.response.EvaluationResponse;
import com.imes.core.service.EvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EvaluationControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        EvaluationController controller = new EvaluationController(evaluationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createEvaluation_ShouldReturn200() throws Exception {
        EvaluationResponse response = sampleResponse("DRAFT");
        when(evaluationService.create(any(CreateEvaluationRequest.class), anyLong())).thenReturn(response);

        CreateEvaluationRequest request = new CreateEvaluationRequest(
                4L, 2L, "MENTOR",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                "draft"
        );

        mockMvc.perform(post("/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void submitEvaluation_ShouldReturnSubmitted() throws Exception {
        when(evaluationService.submit(1L)).thenReturn(sampleResponse("SUBMITTED"));

        mockMvc.perform(post("/evaluations/1/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    void analyticsComparison_ShouldReturn200() throws Exception {
        when(evaluationService.getAll(any(), any(), any(), any())).thenReturn(List.of(sampleResponse("FINALIZED")));

        mockMvc.perform(get("/evaluations")
                        .param("internId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].internProfileId").value(4));
    }

    private EvaluationResponse sampleResponse(String status) {
        return new EvaluationResponse(
                1L,
                4L,
                2L,
                2L,
                "MENTOR",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                status,
                BigDecimal.valueOf(90),
                "A",
                "great",
                LocalDateTime.now(),
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );
    }
}
