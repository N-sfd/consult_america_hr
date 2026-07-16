package com.consultamerica.hr.document.dto;

import com.consultamerica.hr.document.entity.Document;

import java.time.Instant;

public record DocumentResponse(
    Long id,
    Long userId,
    String fileName,
    String contentType,
    String documentType,
    Instant uploadedAt
) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(d.getId(), d.getUserId(), d.getFileName(),
            d.getContentType(), d.getDocumentType(), d.getUploadedAt());
    }
}
