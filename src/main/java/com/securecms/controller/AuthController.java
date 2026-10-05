package com.securecms.controller;

import com.securecms.dto.request.LoginRequest;
import com.securecms.dto.request.RefreshTokenRequest;
import com.securecms.dto.request.RegisterRequest;
import com.securecms.dto.response.ApiResponse;
import com.securecms.dto.response.LoginResponse;
import com.securecms.dto.response.UserResponse;
import com.securecms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, Login, Refresh Token, Logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get access + refresh tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get a new access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (invalidate refresh tokens)")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        if (authentication != null) {
            authService.logout(authentication.getName());
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
