package ru.otus.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.auth.dto.AuthRequest;
import ru.otus.auth.dto.AuthResponse;
import ru.otus.auth.dto.RegisterRequest;
import ru.otus.auth.entity.User;
import ru.otus.auth.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AuthService.class)
class AuthServiceTest {

    @Autowired
    AuthService authService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest("user", "password");

        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashedPwd");
        when(jwtService.generateAccessToken("user")).thenReturn("access.jwt");
        when(jwtService.generateRefreshToken("user")).thenReturn("refresh.jwt");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = authService.register(req);

        assertThat(resp.accessToken()).isEqualTo("access.jwt");
        assertThat(resp.refreshToken()).isEqualTo("refresh.jwt");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_existingUser_shouldThrow() {
        RegisterRequest req = new RegisterRequest("user", "password");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        AuthRequest req = new AuthRequest("user", "pass");
        User user = User.builder().username("user").password("hashedPwd").build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashedPwd")).thenReturn(true);
        when(jwtService.generateAccessToken("user")).thenReturn("access.jwt");
        when(jwtService.generateRefreshToken("user")).thenReturn("refresh.jwt");

        AuthResponse resp = authService.login(req);

        assertThat(resp.accessToken()).isEqualTo("access.jwt");
        assertThat(resp.refreshToken()).isEqualTo("refresh.jwt");
    }

    @Test
    void login_userNotFound_shouldThrow() {
        AuthRequest req = new AuthRequest("user", "pass");
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void login_invalidPassword_shouldThrow() {
        AuthRequest req = new AuthRequest("user", "badpass");
        User user = User.builder().username("user").password("hashedPwd").build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("badpass", "hashedPwd")).thenReturn(false);
        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid credentials");
    }

    // --- REFRESH ---
    @Test
    void refresh_success() {
        String refreshToken = "refresh.jwt";
        User user = User.builder().username("user").password("pwd").build();
        when(jwtService.isValid(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("user")).thenReturn("new.access.jwt");
        when(jwtService.generateRefreshToken("user")).thenReturn("new.refresh.jwt");

        AuthResponse resp = authService.refresh(refreshToken);

        assertThat(resp.accessToken()).isEqualTo("new.access.jwt");
        assertThat(resp.refreshToken()).isEqualTo("new.refresh.jwt");
    }

    @Test
    void refresh_invalidToken_shouldThrow() {
        when(jwtService.isValid("bad.token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh("bad.token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refresh_userNotFound_shouldThrow() {
        when(jwtService.isValid("refresh.jwt")).thenReturn(true);
        when(jwtService.extractUsername("refresh.jwt")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("refresh.jwt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}