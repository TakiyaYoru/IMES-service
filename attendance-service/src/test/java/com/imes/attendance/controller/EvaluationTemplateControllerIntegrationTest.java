package com.imes.attendance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imes.attendance.exception.GlobalExceptionHandler;
import com.imes.common.dto.request.CreateEvaluationTemplateRequest;
import com.imes.common.dto.request.UpdateEvaluationTemplateRequest;
import com.imes.common.dto.response.EvaluationCriteriaResponse;
import com.imes.common.dto.response.EvaluationTemplateResponse;
import com.imes.core.service.EvaluationTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EvaluationTemplateControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private EvaluationTemplateService evaluationTemplateService;

    @BeforeEach
    void setUp() {
        EvaluationTemplateController controller = new EvaluationTemplateController(evaluationTemplateService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTemplate_ShouldReturn200() throws Exception {
        EvaluationTemplateResponse response = sampleTemplateResponse();
        when(evaluationTemplateService.create(any(CreateEvaluationTemplateRequest.class), anyLong())).thenReturn(response);

        CreateEvaluationTemplateRequest request = new CreateEvaluationTemplateRequest(
                "Mentor Template",
                "Monthly mentor template",
                "MENTOR"
        );

        mockMvc.perform(post("/evaluations/templates")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "HR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("Mentor Template"));
    }

    @Test
    void createTemplate_ShouldReturn400_WhenInvalidRequest() throws Exception {
        CreateEvaluationTemplateRequest request = new CreateEvaluationTemplateRequest(
                "",
                "Invalid",
                ""
        );

        mockMvc.perform(post("/evaluations/templates")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "HR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value("3001"));
    }

    @Test
    void getTemplates_ShouldReturnList() throws Exception {
        when(evaluationTemplateService.getAll(eq("MENTOR"))).thenReturn(List.of(sampleTemplateResponse()));

        mockMvc.perform(get("/evaluations/templates")
                        .header("X-User-Role", "MENTOR")
                        .param("evaluationType", "MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10));
    }

    @Test
    void getTemplate_ShouldReturnItem() throws Exception {
        when(evaluationTemplateService.getById(10L)).thenReturn(sampleTemplateResponse());

        mockMvc.perform(get("/evaluations/templates/10")
                        .header("X-User-Role", "MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void updateTemplate_ShouldReturnUpdated() throws Exception {
        when(evaluationTemplateService.update(eq(10L), any(UpdateEvaluationTemplateRequest.class)))
                .thenReturn(sampleTemplateResponse());

        UpdateEvaluationTemplateRequest request = new UpdateEvaluationTemplateRequest(
                "Mentor Template",
                "Updated",
                "MENTOR",
                true
        );

        mockMvc.perform(put("/evaluations/templates/10")
                        .header("X-User-Role", "HR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void deleteTemplate_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(evaluationTemplateService).delete(10L);

        mockMvc.perform(delete("/evaluations/templates/10")
                        .header("X-User-Role", "HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Template deleted successfully"));
    }

    @Test
    void getCriteria_ShouldReturnList() throws Exception {
        when(evaluationTemplateService.getCriteriaByTemplate(10L)).thenReturn(List.of(
                new EvaluationCriteriaResponse(
                        100L,
                        10L,
                        "TECH",
                        "Code Quality",
                        "Clean code",
                        BigDecimal.valueOf(60),
                        10,
                        1
                )
        ));

        mockMvc.perform(get("/evaluations/templates/10/criteria")
                        .header("X-User-Role", "MENTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].criteriaName").value("Code Quality"));
    }

    private EvaluationTemplateResponse sampleTemplateResponse() {
        return new EvaluationTemplateResponse(
                10L,
                "Mentor Template",
                "Monthly mentor template",
                "MENTOR",
                true,
                2L,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
