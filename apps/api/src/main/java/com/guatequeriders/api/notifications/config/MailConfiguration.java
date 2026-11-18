package com.guatequeriders.api.notifications.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class MailConfiguration {

    @Bean
    public JavaMailSender javaMailSender(
            @Value("${MAIL_HOST:localhost}") String host,
            @Value("${MAIL_PORT:1025}") int port,
            @Value("${MAIL_USERNAME:}") String username,
            @Value("${MAIL_PASSWORD:}") String password,
            @Value("${MAIL_SMTP_AUTH:false}") boolean smtpAuth,
            @Value("${MAIL_STARTTLS:false}") boolean startTls,
            @Value("${MAIL_CONNECTION_TIMEOUT:5000}") int connectionTimeout,
            @Value("${MAIL_TIMEOUT:5000}") int timeout,
            @Value("${MAIL_WRITE_TIMEOUT:5000}") int writeTimeout) {

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);

        if (username != null && !username.isBlank()) {
            sender.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            sender.setPassword(password);
        }

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", Boolean.toString(smtpAuth));
        properties.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        properties.put("mail.smtp.connectiontimeout", Integer.toString(connectionTimeout));
        properties.put("mail.smtp.timeout", Integer.toString(timeout));
        properties.put("mail.smtp.writetimeout", Integer.toString(writeTimeout));

        return sender;
    }
}
