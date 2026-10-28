package com.guatequeriders.api.notifications.model;

import java.util.List;

public record EmailNotification(
        List<String> recipients,
        String subject,
        String htmlBody
) {
    public EmailNotification {
        recipients = List.copyOf(recipients);
    }
}
