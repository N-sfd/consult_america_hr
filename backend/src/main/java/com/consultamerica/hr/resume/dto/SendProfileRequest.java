package com.consultamerica.hr.resume.dto;

public record SendProfileRequest(String userEmail, String recipientEmail, String subject, String customMessage) {
}
