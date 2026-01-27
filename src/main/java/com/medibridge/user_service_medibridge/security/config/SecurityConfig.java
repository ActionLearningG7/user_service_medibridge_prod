package com.medibridge.user_service_medibridge.security.config;

import com.medibridge.user_service_medibridge.security.filter.PasswordChangeEnforcementFilter;
import com.medibridge.user_service_medibridge.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for User Service
 * 
 * ARCHITECTURE:
 * - API Gateway handles ALL authentication and authorization
 * - This service TRUSTS the gateway completely
 * - JWT filters are ONLY used for direct auth endpoints (login, register, etc.)
 * - All other endpoints trust X-User-* headers from gateway
 * 
 * IMPORTANT:
 * - Clients should NEVER call this service directly
 * - All requests should go through API Gateway (port 8080)
 * - Gateway adds X-User-Id, X-User-Email, X-User-Role headers
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final PasswordChangeEnforcementFilter passwordChangeEnforcementFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF - Gateway handles this
                                .csrf(AbstractHttpConfigurer::disable)

                                // Disable CORS - Gateway handles this centrally
                                .cors(AbstractHttpConfigurer::disable)

                                // Stateless sessions (JWT-based)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configure exception handling to return 401 instead of redirect
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                                // Disable HTTP Basic authentication popup
                                .httpBasic(AbstractHttpConfigurer::disable)

                                // Disable form login
                                .formLogin(AbstractHttpConfigurer::disable)

                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints (for direct access during auth flow)
                                                .requestMatchers(
                                                                "/api/v1/auth/login",
                                                                "/api/v1/auth/register",
                                                                "/api/v1/auth/refresh",
                                                                "/api/v1/auth/forgot-password",
                                                                "/api/v1/auth/reset-password",
                                                                "/api/v1/auth/verify-email",
                                                                "/actuator/**",
                                                                "/api/v1/organization/**") // Explicitly permit
                                                                                           // organization endpoints
                                                .permitAll()

                                                // All other endpoints trust the gateway
                                                // Gateway has already validated JWT and added X-User-* headers
                                                .anyRequest().permitAll())

                                // Add JWT filter ONLY for auth endpoints
                                // Other endpoints trust gateway headers
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(passwordChangeEnforcementFilter, JwtAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
