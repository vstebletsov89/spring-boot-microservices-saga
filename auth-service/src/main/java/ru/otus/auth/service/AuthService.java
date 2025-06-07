package ru.otus.auth.service;

import com.google.common.hash.BloomFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.otus.auth.dto.AuthRequest;
import ru.otus.auth.dto.AuthResponse;
import ru.otus.auth.dto.RegisterRequest;
import ru.otus.auth.entity.User;
import ru.otus.auth.repository.UserRepository;

import static java.util.Objects.hash;
import static ru.otus.auth.util.PasswordHashUtil.hashPassword;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final BloomFilterManager bloomFilterManager;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(request.username()).isPresent())
            throw new RuntimeException("User already exists");

        var bloomFilter = bloomFilterManager.getFilter();
        if (bloomFilter.mightContain(hashPassword(request.password(), "SHA-256"))) {
            throw new RuntimeException("Password was compromised. Chose another one.");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        String access = jwtService.generateAccessToken(user.getUsername());
        String refresh = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(access, refresh);
    }

    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        String access = jwtService.generateAccessToken(user.getUsername());
        String refresh = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(String refreshToken) {

        if (!jwtService.isValid(refreshToken))
            throw new RuntimeException("Invalid refresh token");

        User user = userRepository.findByUsername(jwtService.extractUsername(refreshToken))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String access = jwtService.generateAccessToken(user.getUsername());
        String newRefresh = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(access, newRefresh);
    }
}
