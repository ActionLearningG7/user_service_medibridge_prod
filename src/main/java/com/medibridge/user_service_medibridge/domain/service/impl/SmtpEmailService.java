package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.domain.service.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * SMTP Email Service using JavaMailSender
 *
 * Configured via environment variables:
 * - SMTP_HOST: SMTP server host (e.g., maildev.railway.internal)
 * - SMTP_PORT: SMTP server port (e.g., 25 for Maildev)
 * - SMTP_USER: SMTP username (optional)
 * - SMTP_PASS: SMTP password (optional)
 * - SMTP_AUTH: Enable authentication (default: false)
 * - SMTP_STARTTLS: Enable STARTTLS (default: false)
 * - MAIL_FROM: From email address (default: no-reply@medibridge.local)
 */
@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host}")
    private String smtpHost;

    @Value("${spring.mail.port}")
    private int smtpPort;

    @Value("${spring.mail.properties.mail.smtp.auth:false}")
    private boolean smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private boolean smtpStartTls;

    @Value("${app.mail.from:no-reply@medibridge.local}")
    private String fromAddress;

    @PostConstruct
    public void logSmtpConfiguration() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📧 SMTP Email Service Initialized");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("   Host:      {}", smtpHost);
        log.info("   Port:      {}", smtpPort);
        log.info("   Auth:      {}", smtpAuth);
        log.info("   STARTTLS:  {}", smtpStartTls);
        log.info("   From:      {}", fromAddress);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Validate configuration
        if (smtpHost == null || smtpHost.isBlank()) {
            log.warn("⚠️  SMTP_HOST is not configured! Email functionality will fail.");
        }

        if ("localhost".equals(smtpHost)) {
            log.info("ℹ️  Using local SMTP server (localhost). For Railway, set SMTP_HOST=maildev.railway.internal");
        } else if (smtpHost.contains("railway.internal")) {
            log.info("✓ Railway Maildev configuration detected");
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            log.debug("Preparing to send email to: {}", to);
            log.debug("Subject: {}", subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("✓ Email sent successfully to: {}", to);
            log.debug("  From: {}", fromAddress);
            log.debug("  Subject: {}", subject);

        } catch (MailException e) {
            log.error("✗ Failed to send email to: {}", to, e);
            log.error("  SMTP Host: {}, Port: {}", smtpHost, smtpPort);
            log.error("  Error: {}", e.getMessage());

            // Don't throw exception to prevent breaking the flow
            // Email delivery failures should not stop user registration/operations
        }
    }

    /**
     * Send HTML email (future enhancement)
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        // For future implementation with MimeMessageHelper
        log.warn("HTML email not yet implemented, falling back to plain text");
        sendEmail(to, subject, htmlBody);
    }
}
