package com.fakenews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fakenews.dto.LoginRequest;
import com.fakenews.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Registration, Login, and Profile workflow integration test")
    void testAuthWorkflow() throws Exception {
        String uniqueEmail = "student_" + System.currentTimeMillis() + "@academic.edu";
        RegisterRequest registerReq = new RegisterRequest("Jane Doe", uniqueEmail, "securePass123");

        // 1. Register
        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andReturn();

        // 2. Login
        LoginRequest loginReq = new LoginRequest(uniqueEmail, "securePass123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 3. Get /api/auth/me using Bearer token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request with field errors when payload is invalid")
    void testRegisterValidationFailure() throws Exception {
        RegisterRequest invalidReq = new RegisterRequest("", "not-an-email", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
}
