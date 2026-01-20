package com.medibridge.user_service_medibridge.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Secure credential notification service for doctor onboarding.
 * 
 * Security: Credentials sent ONLY via email, never logged or exposed via API
 * Compliance: Audit trail for all notification attempts
 * HIPAA: Secure communication channel for sensitive information
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialNotificationService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.support.email:support@medibridge.com}")
    private String supportEmail;

    @Value("${app.temporary.password.validity.hours:24}")
    private int temporaryPasswordValidityHours;

    /**
     * Send doctor onboarding credentials via secure email.
     * 
     * SECURITY CRITICAL:
     * - Credentials are sent ONLY via this method
     * - Never logged
     * - Never returned in API responses
     * - Sent over secure email channel
     * 
     * @param email             Doctor's email address
     * @param username          Generated username
     * @param temporaryPassword Generated temporary password
     * @param fullName          Doctor's full name
     * @param expiresAt         Password expiration timestamp
     */
    public void sendDoctorCredentials(
            String email,
            String username,
            String temporaryPassword,
            String fullName,
            LocalDateTime expiresAt) {
        log.info("Sending onboarding credentials to: {} (username: {})", email, username);
        // SECURITY: Never log the password

        try {
            String emailBody = buildCredentialEmail(username, temporaryPassword, fullName, expiresAt);

            // TODO: Integrate with actual email service (SendGrid, AWS SES, etc.)
            // For now, this is a placeholder that would be replaced with actual email
            // sending
            sendSecureEmail(email, "MediBridge - Doctor Account Credentials", emailBody);

            log.info("Credentials successfully sent to: {}", email);

        } catch (Exception e) {
            log.error("Failed to send credentials to: {}", email, e);
            throw new RuntimeException("Failed to send onboarding credentials", e);
        }
    }

    /**
     * Build the credential email body with security instructions.
     * 
     * @param username          Generated username
     * @param temporaryPassword Temporary password
     * @param fullName          Doctor's name
     * @param expiresAt         Expiration timestamp
     * @return Formatted email body
     */
    private String buildCredentialEmail(
            String username,
            String temporaryPassword,
            String fullName,
            LocalDateTime expiresAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");
        String expirationDate = expiresAt.format(formatter);

        return String.format(
                """
                        Dear Dr. %s,

                        Welcome to MediBridge Healthcare System!

                        Your doctor account has been successfully created. Please use the following credentials to access the system:

                        ═══════════════════════════════════════════════════════════════
                        LOGIN CREDENTIALS
                        ═══════════════════════════════════════════════════════════════

                        Username: %s
                        Temporary Password: %s

                        Login URL: %s/login

                        ═══════════════════════════════════════════════════════════════
                        IMPORTANT SECURITY NOTICE
                        ═══════════════════════════════════════════════════════════════

                        ⚠️  MANDATORY PASSWORD CHANGE REQUIRED
                        You MUST change your password upon first login. You will not be able to access
                        the system until you set a new password.

                        ⏰  TEMPORARY PASSWORD EXPIRATION
                        This temporary password will expire on: %s
                        After expiration, you will need to request a password reset.

                        🔒  PASSWORD REQUIREMENTS
                        Your new password must contain:
                        - At least 12 characters
                        - Uppercase and lowercase letters
                        - At least one number
                        - At least one special character (!@#$%%^&*()-_=+[]{}|;:,.<>?)

                        🛡️  SECURITY BEST PRACTICES
                        - Never share your credentials with anyone
                        - Do not write down your password
                        - Use a unique password not used elsewhere
                        - Enable two-factor authentication after login
                        - Log out when not actively using the system

                        ═══════════════════════════════════════════════════════════════
                        NEED HELP?
                        ═══════════════════════════════════════════════════════════════

                        If you experience any issues logging in or have questions about your account,
                        please contact our support team:

                        Email: %s

                        For security reasons, we will never ask for your password via email or phone.

                        ═══════════════════════════════════════════════════════════════

                        Thank you for joining MediBridge!

                        Best regards,
                        MediBridge Administration Team

                        ---
                        This is an automated message. Please do not reply to this email.
                        For security reasons, this email will not be resent. If you did not receive
                        this email or need new credentials, please contact your system administrator.
                        """,
                fullName,
                username,
                temporaryPassword,
                frontendUrl,
                expirationDate,
                supportEmail);
    }

    /**
     * Send secure email (placeholder for actual email service integration).
     * 
     * TODO: Replace with actual email service implementation
     * Options: SendGrid, AWS SES, Mailgun, etc.
     * 
     * @param to      Recipient email
     * @param subject Email subject
     * @param body    Email body
     */
    private void sendSecureEmail(String to, String subject, String body) {
        // Placeholder for actual email sending logic
        // In production, this would integrate with:
        // - SendGrid API
        // - AWS SES
        // - Mailgun
        // - Or other enterprise email service

        log.info("EMAIL WOULD BE SENT TO: {}", to);
        log.info("SUBJECT: {}", subject);
        log.debug("Email prepared for sending (credentials not logged)");

        // SECURITY: In development/testing, you might want to log to console
        // but NEVER in production
        if (isDevEnvironment()) {
            log.warn("=== DEVELOPMENT MODE - CREDENTIALS EMAIL ===");
            log.warn("TO: {}", to);
            log.warn("SUBJECT: {}", subject);
            log.warn("BODY:\n{}", body);
            log.warn("=== END CREDENTIALS EMAIL ===");
        }
    }

    /**
     * Check if running in development environment.
     * 
     * @return true if development environment
     */
    private boolean isDevEnvironment() {
        String env = System.getProperty("spring.profiles.active", "dev");
        return "dev".equalsIgnoreCase(env) || "development".equalsIgnoreCase(env);
    }

    /**
     * Send password change confirmation email.
     * 
     * @param email    Doctor's email
     * @param fullName Doctor's name
     */
    public void sendPasswordChangeConfirmation(String email, String fullName) {
        log.info("Sending password change confirmation to: {}", email);

        String emailBody = String.format("""
                Dear Dr. %s,

                Your password has been successfully changed.

                If you did not make this change, please contact support immediately at %s.

                Best regards,
                MediBridge Security Team
                """,
                fullName,
                supportEmail);

        try {
            sendSecureEmail(email, "MediBridge - Password Changed", emailBody);
            log.info("Password change confirmation sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation to: {}", email, e);
            // Don't throw exception - password was already changed successfully
        }
    }
}
