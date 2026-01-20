package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.request.*;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.AuthResponse;
import com.medibridge.user_service_medibridge.api.dto.response.UserResponse;
import com.medibridge.user_service_medibridge.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Endpoints: /api/v1/auth
 * Security: Public endpoints (no authentication required)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Register new patient (self-registration)
     * 
     * @param request Patient registration details
     * @return AuthResponse with tokens
     */
    @PostMapping("/register")
    @Operation(summary = "Register new patient", description = "Self-registration for patients only")
    public ResponseEntity<ApiResponse<AuthResponse>> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request) {

        AuthResponse response = authService.registerPatient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Patient registered successfully"));
    }

    /**
     * Login user
     * 
     * @param request Login credentials (email/username + password)
     * @return AuthResponse with tokens
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and generate tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    /**
     * Refresh access token
     * 
     * @param request Refresh token
     * @return AuthResponse with new tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Logout user
     * 
     * @param request Refresh token to revoke
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout", description = "Revoke refresh token and logout user")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    /**
     * Verify email
     * 
     * @param token Email verification token
     * @return UserResponse
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email using token from email")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(
            @RequestParam String token) {

        UserResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(response, "Email verified successfully"));
    }

    /**
     * Forgot password
     * 
     * @param request Email address
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
    }

    /**
     * Reset password
     * 
     * @param request Reset token and new password
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    /**
     * Change password (authenticated users)
     * 
     * @param request        Current and new password
     * @param authentication Current user authentication
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String userId = authentication.getName();
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
