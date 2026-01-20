package com.medibridge.user_service_medibridge.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.exception.error.ErrorResponse;
import com.medibridge.user_service_medibridge.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Security filter to enforce password change on first login.
 * 
 * Purpose: Prevent doctors with temporary passwords from accessing system
 * until they change their password
 * 
 * Security: Enforces mandatory password change for onboarded doctors
 * Compliance: Ensures temporary credentials are not used for system access
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeEnforcementFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Endpoints that are allowed even with temporary password
    private static final List<String> ALLOWED_ENDPOINTS = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/change-password",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String requestPath = request.getRequestURI();

            // Skip filter for allowed endpoints
            if (isAllowedEndpoint(requestPath)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get JWT token from request
            String jwt = getJwtFromRequest(request);

            // If no token, let other filters handle authentication
            if (!StringUtils.hasText(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Validate token and check password change requirement
            if (jwtTokenProvider.validateToken(jwt)) {
                String email = jwtTokenProvider.getEmailFromToken(jwt);

                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null && requiresPasswordChange(user)) {
                    log.warn("Access denied for user {} - password change required", email);
                    sendPasswordChangeRequiredResponse(response, user);
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("Error in password change enforcement filter", ex);
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Check if user requires password change.
     * 
     * @param user User entity
     * @return true if password change is required
     */
    private boolean requiresPasswordChange(User user) {
        // Check if password must be changed
        if (Boolean.TRUE.equals(user.getPasswordMustChange())) {
            log.debug("User {} must change password (mandatory flag set)", user.getEmail());
            return true;
        }

        // Check if temporary password has expired
        if (Boolean.TRUE.equals(user.getPasswordIsTemporary())
                && user.getTemporaryPasswordExpiresAt() != null
                && LocalDateTime.now().isAfter(user.getTemporaryPasswordExpiresAt())) {
            log.warn("Temporary password expired for user {}", user.getEmail());
            return true;
        }

        return false;
    }

    /**
     * Check if the request path is in the allowed list.
     * 
     * @param requestPath Request URI path
     * @return true if endpoint is allowed
     */
    private boolean isAllowedEndpoint(String requestPath) {
        return ALLOWED_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

    /**
     * Extract JWT token from Authorization header.
     * 
     * @param request HTTP request
     * @return JWT token or null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Send password change required error response.
     * 
     * @param response HTTP response
     * @param user     User entity
     * @throws IOException if writing response fails
     */
    private void sendPasswordChangeRequiredResponse(HttpServletResponse response, User user)
            throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = Boolean.TRUE.equals(user.getPasswordIsTemporary())
                && user.getTemporaryPasswordExpiresAt() != null
                && LocalDateTime.now().isAfter(user.getTemporaryPasswordExpiresAt())
                        ? "Your temporary password has expired. Please contact your administrator for a new password."
                        : "You must change your password before accessing the system. Please use the change password endpoint.";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Password Change Required")
                .message(message)
                .path(response.getHeader("X-Request-Path"))
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
