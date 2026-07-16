package com.consultamerica.hr.mail;

public record EmailMessage(
    String to,
    String subject,
    String body,
    byte[] attachmentBytes,
    String attachmentName,
    String attachmentContentType
) {
    public static EmailMessage simple(String to, String subject, String body) {
        return new EmailMessage(to, subject, body, null, null, null);
    }
}
