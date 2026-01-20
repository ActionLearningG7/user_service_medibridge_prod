package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.domain.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogEmailService implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("========== MOCK EMAIL ==========");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("================================");
    }
}
