package com.fakenews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fakenews.dto.AnalyzeRequest;
import com.fakenews.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AnalysisController Integration Tests")
class AnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Health endpoint should return 200 OK")
    void testActuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Should reject empty news text with 400 Bad Request")
    void testAnalyzeEmptyTextValidation() throws Exception {
        AnalyzeRequest req = new AnalyzeRequest("Headline", "   ", null, null);

        mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.text").exists());
    }

    @Test
    @DisplayName("Should reject malformed source URL with 400 Bad Request (SSRF protection)")
    void testAnalyzeMalformedUrl() throws Exception {
        AnalyzeRequest req = new AnalyzeRequest(
                "Headline",
                "Valid length text that contains enough words to satisfy requirements.",
                "javascript:alert(1)",
                null
        );

        mockMvc.perform(post("/api/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.sourceUrl").exists());
    }

    @Test
    @DisplayName("Authenticated user can submit analysis, retrieve it from history, and delete it")
    void testFullAnalysisAndHistoryLifecycle() throws Exception {
        String email = "analyst_" + System.currentTimeMillis() + "@academic.edu";
        RegisterRequest registerReq = new RegisterRequest("Analyst User", email, "secret123");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString())
                .get("token").asText();

        // 1. Submit analysis with Bearer token
        AnalyzeRequest analyzeReq = new AnalyzeRequest(
                "Economic Growth Surge",
                "The Federal Reserve announced positive quarter GDP statistics during their regular briefing in Washington.",
                "https://example.com/news/economy",
                "Economy"
        );

        MvcResult analyzeResult = mockMvc.perform(post("/api/analyses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(analyzeReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.label").isString())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.explanation").isString())
                .andReturn();

        Long analysisId = objectMapper.readTree(analyzeResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. Fetch paginated history
        mockMvc.perform(get("/api/analyses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(analysisId));

        // 3. Fetch specific analysis by ID
        mockMvc.perform(get("/api/analyses/" + analysisId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(analysisId));

        // 4. Check dashboard summary
        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalyses").value(1));

        // 5. Delete analysis
        mockMvc.perform(delete("/api/analyses/" + analysisId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 6. Verify deleted
        mockMvc.perform(get("/api/analyses/" + analysisId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Unauthenticated request to history endpoint must return 401 Unauthorized")
    void testUnauthorizedHistoryAccess() throws Exception {
        mockMvc.perform(get("/api/analyses"))
                .andExpect(status().isUnauthorized());
    }
}
