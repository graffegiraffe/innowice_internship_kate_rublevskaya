package by.rublevskaya.authservice.service;

import by.rublevskaya.authservice.dto.LoginRequest;
import by.rublevskaya.authservice.dto.RefreshTokenRequest;
import by.rublevskaya.authservice.dto.RegisterRequest;
import by.rublevskaya.authservice.dto.TokenResponse;
import by.rublevskaya.authservice.dto.TokenValidationResponse;
import by.rublevskaya.authservice.dto.ValidateTokenRequest;
import by.rublevskaya.authservice.entity.RefreshToken;
import by.rublevskaya.authservice.entity.UserCredential;
import by.rublevskaya.authservice.exception.AuthenticationException;
import by.rublevskaya.authservice.exception.TokenException;
import by.rublevskaya.authservice.exception.UserAlreadyExistsException;
import by.rublevskaya.authservice.repository.RefreshTokenRepository;
import by.rublevskaya.authservice.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {
        log.info("Registering new user with login: {}", request.getLogin());

        if (userCredentialRepository.existsByLogin(request.getLogin())) {
            throw new UserAlreadyExistsException("Login already exists: " + request.getLogin());
        }

        if (userCredentialRepository.existsByUserId(request.getUserId())) {
            throw new UserAlreadyExistsException("User ID already registered: " + request.getUserId());
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        UserCredential userCredential = UserCredential.builder()
                .login(request.getLogin())
                .password(hashedPassword)
                .userId(request.getUserId())
                .role(request.getRole())
                .build();

        userCredentialRepository.save(userCredential);
        log.info("User registered successfully: {}", request.getLogin());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getLogin());

        UserCredential userCredential = userCredentialRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), userCredential.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(userCredential);
        String refreshToken = jwtService.generateRefreshToken(userCredential.getLogin());

        saveRefreshToken(userCredential.getUserId(), refreshToken);

        log.info("User logged in successfully: {}", request.getLogin());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request");

        String refreshTokenValue = request.getRefreshToken();

        if (!jwtService.isTokenValid(refreshTokenValue)) {
            throw new TokenException("Invalid refresh token");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException("Refresh token expired");
        }

        String login = jwtService.extractLogin(refreshTokenValue);
        UserCredential userCredential = userCredentialRepository.findByLogin(login)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(userCredential);
        String newRefreshToken = jwtService.generateRefreshToken(login);

        refreshTokenRepository.delete(refreshToken);
        saveRefreshToken(userCredential.getUserId(), newRefreshToken);

        log.info("Token refreshed successfully for user: {}", login);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(ValidateTokenRequest request) {
        String token = request.getToken();

        try {
            if (!jwtService.isTokenValid(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid token")
                        .build();
            }

            if (jwtService.isTokenExpired(token)) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Token expired")
                        .build();
            }

            Long userId = jwtService.extractUserId(token);
            String login = jwtService.extractLogin(token);
            String role = jwtService.extractRole(token);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .login(login)
                    .role(role)
                    .message("Token is valid")
                    .build();

        } catch (Exception e) {
            log.error("Token validation error", e);
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    private void saveRefreshToken(Long userId, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
    }
}