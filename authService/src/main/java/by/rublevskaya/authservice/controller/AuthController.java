package by.rublevskaya.authservice.controller;

import by.rublevskaya.authservice.dto.LoginRequest;
import by.rublevskaya.authservice.dto.RefreshTokenRequest;
import by.rublevskaya.authservice.dto.RegisterRequest;
import by.rublevskaya.authservice.dto.TokenResponse;
import by.rublevskaya.authservice.dto.TokenValidationResponse;
import by.rublevskaya.authservice.dto.ValidateTokenRequest;
import by.rublevskaya.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and Authorization API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register new user credentials",
            description = "Creates new user credentials with hashed password"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @Operation(
            summary = "Login",
            description = "Authenticates user and returns JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates new access and refresh tokens using refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(
            summary = "Validate token",
            description = "Validates JWT token and returns user information"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token validated"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Valid @RequestBody ValidateTokenRequest request) {
        TokenValidationResponse response = authService.validateToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Health check",
            description = "Check if auth service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
