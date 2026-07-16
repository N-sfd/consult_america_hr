package com.consultamerica.hr.job.dto;

public record JobPostRequest(
    String title,
    String description,
    String location,
    String employmentType,
    String technologyStack,
    String clientName,
    String contactEmail,
    String postedAt
) {
}
