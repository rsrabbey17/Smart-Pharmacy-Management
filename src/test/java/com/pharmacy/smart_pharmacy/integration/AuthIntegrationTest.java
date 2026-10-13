package com.pharmacy.smart_pharmacy.integration;

import com.pharmacy.smart_pharmacy.dto.request.LoginRequest;
import com.pharmacy.smart_pharmacy.dto.request.RegisterRequest;
import com.pharmacy.smart_pharmacy.entity.Role;
import com.pharmacy.smart_pharmacy.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;                          // ← Jackson 3
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void fullFlow_registerThenLogin_shouldSucceed() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername("integration_user");
        registerReq.setEmail("integration@test.com");
        registerReq.setPassword("password123");
        registerReq.setFullName("Integration Test");
        registerReq.setRole(Role.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("integration_user"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        assertThat(userRepository.findByUsername("integration_user")).isPresent();
        assertThat(userRepository.findByUsername("integration_user").get().getPassword())
                .startsWith("$2a$")
                .isNotEqualTo("password123");

        LoginRequest loginReq = new LoginRequest("integration_user", "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("integration_user"))
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(body)
                .path("data").path("accessToken").asText();

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void login_shouldFail_whenPasswordWrong() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername("wrong_pw_user");
        registerReq.setEmail("wrong@test.com");
        registerReq.setPassword("correctPassword");
        registerReq.setRole(Role.CASHIER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        LoginRequest badLogin = new LoginRequest("wrong_pw_user", "WRONG_PASSWORD");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void register_shouldFail_whenUsernameDuplicate() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("dup_user");
        req.setEmail("dup1@test.com");
        req.setPassword("password123");
        req.setRole(Role.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        req.setEmail("dup2@test.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }
}