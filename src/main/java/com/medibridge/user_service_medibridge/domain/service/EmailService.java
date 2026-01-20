package com.medibridge.user_service_medibridge.domain.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
}
