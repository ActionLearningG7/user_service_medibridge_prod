package com.medibridge.user_service_medibridge.domain.service;

import com.medibridge.user_service_medibridge.api.dto.request.*;
import com.medibridge.user_service_medibridge.api.dto.response.AuthResponse;
import com.medibridge.user_service_medibridge.api.dto.response.UserResponse;

/**
 * Service interface for authentication operations.
 * 
 * Security: Handles user authentication, registration, and token management
 */
public interface AuthService {

    /**
     * Register a new patient (self-registration)
     */
    AuthResponse registerPatient(RegisterPatientRequest request);

    /**
     * Authenticate user and generate tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh access token using refresh token
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout user and revoke tokens
     */
    void logout(String refreshToken);

    /**
     * Verify email using verification token
     */
    UserResponse verifyEmail(String token);

    /**
     * Initiate forgot password flow
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password using reset token
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Change password for authenticated user
     */
    void changePassword(String userId, ChangePasswordRequest request);
}
