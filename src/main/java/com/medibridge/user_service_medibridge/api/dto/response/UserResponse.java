package com.medibridge.user_service_medibridge.api.dto.response;

import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for User entity.
 * 
 * Security: Excludes sensitive fields (password, tokens)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID userId;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean twoFactorEnabled;
    private Boolean isAdmin;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
