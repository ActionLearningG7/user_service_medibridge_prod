package com.medibridge.user_service_medibridge.exception.handler;

import com.medibridge.user_service_medibridge.exception.custom.*;
import com.medibridge.user_service_medibridge.exception.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all controllers
 * 
 * Provides consistent error responses across the application
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFoundException(
                        UserNotFoundException ex, HttpServletRequest request) {

                log.error("User not found: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Not Found")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(ProfileNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleProfileNotFoundException(
                        ProfileNotFoundException ex, HttpServletRequest request) {

                log.error("Profile not found: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .error("Not Found")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(DuplicateEmailException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
                        DuplicateEmailException ex, HttpServletRequest request) {

                log.error("Duplicate email: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .error("Conflict")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler({ InvalidCredentialsException.class, BadCredentialsException.class })
        public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
                        Exception ex, HttpServletRequest request) {

                log.error("Invalid credentials: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message("Invalid email/username or password")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(AccountLockedException.class)
        public ResponseEntity<ErrorResponse> handleAccountLockedException(
                        AccountLockedException ex, HttpServletRequest request) {

                log.error("Account locked: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        @ExceptionHandler({ UnauthorizedException.class, AccessDeniedException.class })
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        Exception ex, HttpServletRequest request) {

                log.error("Unauthorized access: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message("Unauthorized access")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(InsufficientPermissionException.class)
        public ResponseEntity<ErrorResponse> handleInsufficientPermissionException(
                        InsufficientPermissionException ex, HttpServletRequest request) {

                log.error("Insufficient permissions: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Forbidden")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ErrorResponse> handleInvalidTokenException(
                        InvalidTokenException ex, HttpServletRequest request) {

                log.error("Invalid token: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("Unauthorized")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                log.error("Validation failed: {}", ex.getMessage());

                List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> ErrorResponse.ValidationError.builder()
                                                .field(error.getField())
                                                .message(error.getDefaultMessage())
                                                .rejectedValue(error.getRejectedValue())
                                                .build())
                                .collect(Collectors.toList());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Failed")
                                .message("Invalid input data")
                                .path(request.getRequestURI())
                                .validationErrors(validationErrors)
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(UnsupportedOperationException.class)
        public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(
                        UnsupportedOperationException ex, HttpServletRequest request) {

                log.error("Operation not implemented: {}", ex.getMessage());

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.NOT_IMPLEMENTED.value())
                                .error("Not Implemented")
                                .message("This feature is not yet implemented")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
        }

        @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
                        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                log.error("Invalid parameter type: {} - value: {}", ex.getName(), ex.getValue());

                String message = String.format(
                                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                                ex.getValue(),
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(message)
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                log.error("Unexpected error: ", ex);

                ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("Internal Server Error")
                                .message("An unexpected error occurred")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
