package ru.otus.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.auth.dto.AuthRequest;
import ru.otus.auth.dto.AuthResponse;
import ru.otus.auth.dto.RegisterRequest;
import ru.otus.auth.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @Test
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest("user", "pass");
        AuthResponse resp = new AuthResponse("access.jwt", "refresh.jwt");
        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.jwt"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_success() throws Exception {
        AuthRequest req = new AuthRequest("user", "pass");
        AuthResponse resp = new AuthResponse("access.jwt", "refresh.jwt");
        when(authService.login(any(AuthRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.jwt"));

        verify(authService).login(any(AuthRequest.class));
    }

    @Test
    void refresh_success() throws Exception {
        String refreshToken = "refresh.jwt";
        AuthResponse resp = new AuthResponse("new.access.jwt", "new.refresh.jwt");
        when(authService.refresh(anyString())).thenReturn(resp);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh.jwt"));

        verify(authService).refresh(anyString());
    }

    @Test
    void login_failure() throws Exception {
        AuthRequest req = new AuthRequest("user", "wrong");
        when(authService.login(any(AuthRequest.class))).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_failure_user_not_found() throws Exception {
        AuthRequest req = new AuthRequest("user", "wrong");
        when(authService.login(any(AuthRequest.class))).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void refresh_failure_user_not_found() throws Exception {
        when(authService.refresh(any(String.class))).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"test\"}"))
                .andExpect(status().isNotFound());
    }
}