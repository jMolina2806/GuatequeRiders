package com.guatequeriders.api.notifications.service;

import com.guatequeriders.api.notifications.model.EmailNotification;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.enabled:false}") boolean enabled,
                        @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void send(EmailNotification notification) {
        if (!enabled) {
            log.debug("Email notifications disabled: subject={}", notification.subject());
            return;
        }

        for (String recipient : new LinkedHashSet<>(notification.recipients())) {
            if (recipient == null || recipient.isBlank()) {
                continue;
            }

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                helper.setFrom(from);
                helper.setTo(recipient.trim());
                helper.setSubject(notification.subject());
                helper.setText(notification.htmlBody(), true);
                mailSender.send(message);
                log.info("Email notification sent: recipient={}, subject={}", recipient, notification.subject());
            } catch (Exception exception) {
                log.error("Email notification failed: recipient={}, subject={}",
                        recipient, notification.subject(), exception);
            }
        }
    }
}
