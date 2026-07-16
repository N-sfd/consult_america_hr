package com.consultamerica.hr.resume.dto;

import com.consultamerica.hr.resume.entity.Resume;

import java.time.Instant;
import java.util.List;

public record ResumeResponse(
    Long id,
    String name,
    String email,
    String contact,
    String title,
    String summary,
    String visaStatus,
    String linkedln,
    List<String> tags,
    String fileName,
    boolean hasFile,
    Instant createdAt
) {
    public static ResumeResponse from(Resume r) {
        return new ResumeResponse(
            r.getId(), r.getName(), r.getEmail(), r.getContact(), r.getTitle(),
            r.getSummary(), r.getVisaStatus(), r.getLinkedln(), r.getTags(),
            r.getFileName(), r.getFileBytes() != null && r.getFileBytes().length > 0,
            r.getCreatedAt());
    }
}
