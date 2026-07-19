package com.consultamerica.hr.mail;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailProperties properties;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider, EmailProperties properties) {
        this.mailSenderProvider = mailSenderProvider;
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.isEnabled() && mailSenderProvider.getIfAvailable() != null;
    }

    public void send(EmailMessage message) {
        if (!isConfigured()) {
            log.warn("Email not configured; skipping send to {} subject='{}'", message.to(), message.subject());
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getObject();
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,
                message.attachmentBytes() != null, "UTF-8");
            String from = Objects.requireNonNull(properties.getFrom(), "email.from must be configured");
            String to = Objects.requireNonNull(message.to(), "email recipient is required");
            String subject = message.subject() == null ? "" : message.subject();
            String body = message.body() == null ? "" : message.body();

            helper.setFrom(from);
            helper.setTo(to);
            java.util.Objects.requireNonNull(subject);
            java.util.Objects.requireNonNull(body);
            helper.setSubject(subject);
            helper.setText(body, false);
            if (message.attachmentBytes() != null && message.attachmentName() != null) {
                helper.addAttachment(Objects.requireNonNull(message.attachmentName()),
                    new org.springframework.core.io.ByteArrayResource(Objects.requireNonNull(message.attachmentBytes())));
            }
            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", message.to(), ex.getMessage());
        }
    }

    public void sendPasswordReset(String toEmail, String resetLink) {
        String body = "We received a request to reset your password.\n\n"
            + "Click the link below to choose a new password (this link expires in 30 minutes):\n"
            + resetLink + "\n\n"
            + "If you didn't request this, you can safely ignore this email.";
        send(EmailMessage.simple(toEmail, "Reset your password", body));
    }

    public void sendResumeProfile(String recipientEmail, String subject, String customMessage,
                                   byte[] fileBytes, String fileName, String fileContentType) {
        String body = (customMessage == null || customMessage.isBlank())
            ? "Please find the attached candidate profile."
            : customMessage;
        String effectiveSubject = (subject == null || subject.isBlank()) ? "Candidate Profile" : subject;
        send(new EmailMessage(recipientEmail, effectiveSubject, body, fileBytes, fileName, fileContentType));
    }

    public void sendProfileCompleteNotification(String candidateEmail, Map<String, Object> payload) {
        StringBuilder body = new StringBuilder("A candidate profile update was submitted.\n\n");
        if (payload != null) {
            payload.forEach((k, v) -> body.append(k).append(": ").append(v).append('\n'));
        }
        send(EmailMessage.simple(candidateEmail, "Profile Update Notification", body.toString()));
    }
}
